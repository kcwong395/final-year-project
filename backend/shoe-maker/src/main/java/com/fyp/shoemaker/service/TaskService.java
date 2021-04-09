package com.fyp.shoemaker.service;

import com.fyp.shoemaker.model.Elf;
import com.fyp.shoemaker.model.Document;
import com.fyp.shoemaker.model.Task;
import com.fyp.shoemaker.model.Record;
import com.fyp.shoemaker.repository.RecordRepository;
import com.fyp.shoemaker.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ElfService elfService;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ComputationService computationService;

    @Value("${storagePrefix}")
    private String storage;

    public static ConcurrentHashMap<UUID, Thread> taskThread = new ConcurrentHashMap<>();

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Page<Task> getTasksWithPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return taskRepository.findAll(pageable);
    }

    public Optional<Task> getTask(UUID id) {
        return taskRepository.findById(id);
    }

    public void clearTask(UUID taskId) {
        Path path = Paths.get(storage + taskId.toString());
        List<File> fl = new ArrayList<>();
        for(File f : path.toFile().listFiles()) {
            String fn = f.toString();
            System.out.println(fn);
            if(!fn.contains("A.csv") && !fn.contains("B.csv") && !fn.contains("R.csv")) {
                fl.add(f);
            }
        }
        fileService.deleteFiles(fl.toArray(new File[fl.size()]));
    }

    public Task submitTask(Task task) {
        // assign elves to this task and set the state
        Task returnTask = taskRepository.save(task);
        List<UUID> elvesOnDuty = elfService.assignElvesToTask(returnTask.getN());
        if(elvesOnDuty == null) {
            returnTask.setStatus(Task.Status.Error);
            recordRepository.save(new Record("[Task Error]", "Not enough available elves", Record.Type.Danger, returnTask.getId(), null));
        }
        else {
            returnTask.setStatus((Task.Status.Created));
            returnTask.setElvesOnDuty(elvesOnDuty);
        }
        returnTask = taskRepository.save(returnTask);

        // TODO: Use ThreadPool
        Task finalReturnTask = returnTask;
        Thread t = new Thread(() -> {

            final int MAX_TIMEOUT = 15;
            LocalDateTime timeOut = LocalDateTime.now();
            Task newTask = finalReturnTask;

            // wait until all files are uploaded
            while(true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(fileService.getFilesUnderTask(newTask.getId()).size() >= 2) {
                    break;
                }
                else if(Duration.between(timeOut, LocalDateTime.now()).toMinutes() > MAX_TIMEOUT) {
                    // Thread will not wait for file to upload forever, it shall quit as it reach the timeout
                    newTask = getTask(newTask.getId()).get();
                    newTask.setStatus(Task.Status.Error);
                    taskRepository.save(newTask);
                    recordRepository.save(new Record("[Task Error]", "File Submission Timeout", Record.Type.Danger, newTask.getId(), null));
                    elfService.freeElvesFromTask(finalReturnTask.getElvesOnDuty());
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            newTask = getTask(newTask.getId()).get();
            newTask.setStatus(Task.Status.Started);
            taskRepository.save(newTask);

            recordRepository.save(new Record("[Task Started]", newTask.getId().toString(), Record.Type.Info, newTask.getId(), null));

            // Start computation
            List<Elf> elves = new ArrayList<>();
            for(UUID id : newTask.getElvesOnDuty()) {
                elves.add(elfService.getElf(id).get());
            }
            computationService.workInBatch(newTask, elves);

            newTask = getTask(newTask.getId()).get();
            newTask.setEndTime(LocalDateTime.now());
            newTask.setDuration(Duration.between(newTask.getCreateTime(), newTask.getEndTime()).toMillis());
            newTask.setStatus(Task.Status.Completed);
            taskRepository.save(newTask);
            fileService.addFile(new Document(newTask.getId(), storage, "Result.csv", 'R'));
            recordRepository.save(new Record("[Task Completed]", newTask.getId().toString(), Record.Type.Success, newTask.getId(), null));
            clearTask(newTask.getId());
            elfService.freeElvesFromTask(newTask.getElvesOnDuty());

        });
        t.start();
        taskThread.put(returnTask.getId(), t);
        recordRepository.save(new Record("[Task Created]", returnTask.getId().toString(), Record.Type.Normal, returnTask.getId(), null));

        return returnTask;
    }

    public void cancelTask(UUID id) {
        if(id != null && getTask(id).isPresent()) {
            Task task = getTask(id).get();
            if(task.getStatus() != Task.Status.Completed && task.getStatus() != Task.Status.Error) {
                task.setStatus(Task.Status.Canceled);
                taskRepository.save(task);
                recordRepository.save(new Record("[Task Canceled]", task.getId().toString(), Record.Type.Warning, task.getId(), null));
            }
        }
    }

    public void pauseTask(UUID id) {
        if(id != null && getTask(id).isPresent()) {
            Task task = getTask(id).get();
            if(task.getStatus() == Task.Status.Started) {
                task.setStatus(Task.Status.Paused);
                taskRepository.save(task);
                recordRepository.save(new Record("[Task Paused]", task.getId().toString(), Record.Type.Warning, task.getId(), null));
            }
            else if(task.getStatus() == Task.Status.Paused) {
                task.setStatus(Task.Status.Started);
                taskRepository.save(task);
                recordRepository.save(new Record("[Task Resumed]", task.getId().toString(), Record.Type.Warning, task.getId(), null));
            }
        }
    }
}
