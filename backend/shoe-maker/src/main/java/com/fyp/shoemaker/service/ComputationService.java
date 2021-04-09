package com.fyp.shoemaker.service;

import com.fyp.shoemaker.model.Elf;
import com.fyp.shoemaker.model.NodeHandler;
import com.fyp.shoemaker.model.Record;
import com.fyp.shoemaker.model.Task;
import com.fyp.shoemaker.repository.ElfRepository;
import com.fyp.shoemaker.repository.RecordRepository;
import com.fyp.shoemaker.repository.TaskRepository;
import com.fyp.shoemaker.util.FileUtil;
import com.fyp.shoemaker.util.MatrixUtil;
import org.jblas.DoubleMatrix;
import org.jblas.ranges.IntervalRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ComputationService {

    @Value("${storagePrefix}")
    private String storage;

    @Autowired
    private FileService fileService;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    public void workInBatch(Task task, List<Elf> elves) {
        String taskPath = storage + task.getId() + "/";

        String pathToA = taskPath + "A.csv";

        Map<String, Long> timeMap = new ConcurrentHashMap<>();
        timeMap.put("fill0Rows", (long) 0);
        timeMap.put("partition", (long) 0);
        timeMap.put("encode", (long) 0);
        timeMap.put("decode", (long) 0);

        // divide the matrix by batch
        try {
            File csvFile = new File(pathToA);

            if (csvFile.isFile()) {
                String line = "";
                try (BufferedReader csvReader = new BufferedReader(new FileReader(pathToA))) {
                    while (true) {
                        int numOfLine = 0;
                        try (FileWriter writer = new FileWriter(taskPath + "A-tmp.csv")) {
                            // TODO: Change this threshold to dynamic
                            while (numOfLine < 500) {
                                line = csvReader.readLine();
                                if (line == null) break;
                                writer.append(line).append(String.valueOf('\n'));
                                numOfLine++;
                            }
                        }

                        // calculate how many batch is needed
                        if(task.getBatch() == 0) {
                            long fullSize = new File(taskPath + "A.csv").length();
                            long batchSize = new File(taskPath + "A-tmp.csv").length();
                            task = taskService.getTask(task.getId()).get();
                            task.setTotalBatch((int)(fullSize / batchSize));
                            taskRepository.save(task);
                        }

                        if (taskService.getTask(task.getId()).get().getStatus() == Task.Status.Canceled || numOfLine == 0) {
                            break;
                        }

                        while(taskService.getTask(task.getId()).get().getStatus() == Task.Status.Paused) {
                            Thread.sleep(5000);
                        }

                        // compute it batch by batch
                        if(Task.Approach.CodeApp.equals(task.getApproach())) {
                            codedApproach(task, elves, timeMap);
                        }
                        else {
                            distributedApproach(task, elves);
                        }

                        task = taskService.getTask(task.getId()).get();
                        task.setBatch(task.getBatch() + 1);
                        if(task.getBatch() > task.getTotalBatch()) {
                            task.setTotalBatch(task.getBatch());
                        }
                        task.setTimeMap(timeMap);
                        taskRepository.save(task);
                        recordRepository.save(new Record("[Batch-" + task.getBatch() + " Completed]", task.getId().toString(), Record.Type.Info, task.getId(), null));
                    }
                }
            } else {
                System.out.println("File not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void distributedApproach(Task task, List<Elf> elves) {
        String taskPath = storage + task.getId() + "/";
        try {
            // TODO: divide the matrix without parsing as matrix
            // File partitioning
            FileUtil.partitionFile(taskPath + "A-tmp.csv", task.getN());

            // File distribution
            List<NodeHandler> threads = new ArrayList<>();
            Map<Integer, DoubleMatrix> ansList = new ConcurrentHashMap<>();
            for(int i = 0; i < elves.size(); i++) {
                NodeHandler t = new NodeHandler(task, elves, i, storage, fileService, recordRepository, ansList);
                t.start();
                threads.add(t);
            }

            // Wait all threads to complete and Aggregate the result
            for(NodeHandler t : threads) {
                t.join();
            }

            // String[] path = new String[task.getN()];
            for(int i = 0; i < task.getN(); i++) {
                // path[i] = taskPath + "R-" + i + ".csv";
                MatrixUtil.saveCSV(ansList.get(i), storage + task.getId().toString() + "/" + "R.csv", true);
            }
            //FileUtil.mergeFile(path, taskPath + "R.csv");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void codedApproach(Task task, List<Elf> elves, Map<String, Long> timeMap) {

        String taskPath = storage + task.getId() + "/";
        Long delay;

        try {
            DoubleMatrix A = DoubleMatrix.loadCSVFile(taskPath + "A-tmp.csv");

            // Fill 0 row
            LocalDateTime start = LocalDateTime.now();
            DoubleMatrix f0A = MatrixUtil.fill0Rows(A, task.getK());
            delay = Duration.between(start, LocalDateTime.now()).toMillis();

            recordRepository.save(new Record("[Fill 0 Rows]", String.valueOf(delay), Record.Type.Info, task.getId(), null));
            timeMap.put("fill0Rows", timeMap.get("fill0Rows") + delay);

            // Partition
            start = LocalDateTime.now();
            DoubleMatrix[] partitA = MatrixUtil.partitionMatrix(f0A, task.getK());
            delay = Duration.between(start, LocalDateTime.now()).toMillis();
            recordRepository.save(new Record("[Partition]", String.valueOf(delay), Record.Type.Info, task.getId(), null));
            timeMap.put("partition", timeMap.get("partition") + delay);

            // Encode
            start = LocalDateTime.now();
            DoubleMatrix encMat = DoubleMatrix.loadCSVFile(storage + "RSCode_" + task.getN() + "_" + task.getK() + ".csv").transpose();

            DoubleMatrix[] encA = MatrixUtil.encodeMatrix(task.getN(), task.getK(), partitA, encMat);
            for(int i = 0; i < task.getN(); i++) {
                MatrixUtil.saveCSV(encA[i], storage + task.getId() + "/" + "A-" + i + ".csv", false);
            }
            delay = Duration.between(start, LocalDateTime.now()).toMillis();
            recordRepository.save(new Record("[Encode]", String.valueOf(delay), Record.Type.Info, task.getId(), null));
            timeMap.put("encode", timeMap.get("encode") + delay);

            start = LocalDateTime.now();
            Map<Integer, DoubleMatrix> ansList = new ConcurrentHashMap<>();
            List<NodeHandler> threads = new ArrayList<>();
            for(int i = 0; i < elves.size(); i++) {
                NodeHandler t = new NodeHandler(task, elves, i, storage, fileService, recordRepository, ansList);
                t.start();
                threads.add(t);
            }

            while(ansList.size() < task.getK()) {
                Thread.sleep(100);
            }
            recordRepository.save(new Record("[Wait k Completed]", String.valueOf(Duration.between(start, LocalDateTime.now()).toMillis()), Record.Type.Info, task.getId(), null));

            start = LocalDateTime.now();
            DoubleMatrix res = MatrixUtil.decodeMatrix(encMat, task.getN(), task.getK(), ansList);

            IntervalRange r = new IntervalRange(0, A.rows);
            IntervalRange c = new IntervalRange(0, res.columns);
            res = res.get(r, c);
            delay = Duration.between(start, LocalDateTime.now()).toMillis();
            recordRepository.save(new Record("[Decode]", String.valueOf(delay), Record.Type.Info, task.getId(), null));
            timeMap.put("decode", timeMap.get("decode") + delay);

            MatrixUtil.saveCSV(res, storage + task.getId().toString() + "/" + "R.csv", true);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
