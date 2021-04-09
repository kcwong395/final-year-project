package com.fyp.shoemaker.model;

import com.fyp.shoemaker.model.Elf;
import com.fyp.shoemaker.model.Task;
import com.fyp.shoemaker.repository.RecordRepository;
import com.fyp.shoemaker.service.FileService;
import com.fyp.shoemaker.util.RequestUtil;
import lombok.Value;
import org.jblas.DoubleMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.util.List;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeHandler extends Thread {

    private List<Elf> elves;
    private int position;
    private Task task;
    private FileService fileService;
    private RecordRepository recordRepository;
    private String storage;
    private Map<Integer, DoubleMatrix> map;

    public NodeHandler(Task task, List<Elf> elves, int position, String storage, FileService fileService, RecordRepository recordRepository, Map<Integer, DoubleMatrix> ans) {
        this.elves = elves;
        this.task = task;
        this.position = position;
        this.fileService = fileService;
        this.recordRepository = recordRepository;
        this.storage = storage;
        this.map = ans;
    }

    public void run() {
        try {

            Elf elf = elves.get(position);

            // Inject a delay
            if(position <= 0) {
                int delay = 1000;
                Thread.sleep(delay);
                recordRepository.save(new Record("[Elf-" + position + " Delayed]", String.valueOf(delay), Record.Type.Info, task.getId(), null));
            }

            recordRepository.save(new Record("[Elf-" + position + " Started]", String.valueOf(LocalDateTime.now()), Record.Type.Info, task.getId(), null));
            LocalDateTime time = LocalDateTime.now();

            // send request to submit task to elf and create a folder to contain files
            //System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS")) + " [Task Distributed] : " + elf.toString());
            String url = "http:/" + elf.getIp().toString() + ":" + elf.getPort() + "/api/task/" + task.getId().toString();
            RequestUtil.initiateRequest("POST", url, "");

            // send the file to elf
            String src = storage + task.getId().toString() + "/B.csv " + storage + task.getId().toString() + "/A-" + position + ".csv";
            //String dest = "node@" + elf.getIp().toString().split("/")[1] + ":/home/node/fyp/" + storage + task.getId().toString();
            String dest = "root@" + elf.getIp().toString().split("/")[1] + ":" + storage + task.getId().toString();
            recordRepository.save(new Record("[File Transfer]", src + " " + dest, Record.Type.Info, task.getId(), null));
            fileService.fileTransfer(src, dest);

            // send request to submit task to elf
            //System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS")) + " [Task Distributed] : " + elves.get(position).toString());
            url += "/" + position;
            RequestUtil.initiateRequest("POST", url, "");

            // retrieve result from elf
            //src = "node@" + elf.getIp().toString().split("/")[1] + ":/home/node/fyp/" + storage + task.getId().toString() + "/R-" + position + ".csv";
            src = "root@" + elf.getIp().toString().split("/")[1] + ":" + storage + task.getId().toString() + "/R-" + position + ".csv";
            dest = storage + task.getId().toString();
            recordRepository.save(new Record("[File Transfer]", src + " " + dest, Record.Type.Info, task.getId(), null));
            fileService.fileTransfer(src, dest);

            if(map != null) {
                map.put(position, DoubleMatrix.loadCSVFile(storage + task.getId().toString() + "/R-" + position + ".csv"));
            }
            recordRepository.save(new Record("[Elf-" + position + " Completed]", String.valueOf(Duration.between(time, LocalDateTime.now()).toMillis()), Record.Type.Info, task.getId(), null));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
