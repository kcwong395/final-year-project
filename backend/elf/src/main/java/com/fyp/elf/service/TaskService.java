package com.fyp.elf.service;

import com.fyp.elf.util.MatrixUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory;


@Service
public class TaskService {

    @Value("${storagePrefix}")
    private String storage;

    // After receiving the task, create a directory so that master can copy the files to it
    public void createTask(UUID id) {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS")) + " [Task Start] : " + id);
        Path path = Paths.get(storage + id.toString());
        try {
            Files.createDirectory(path);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void processTask(UUID id, int position) {
        try {
            String path = storage + id.toString();
            LocalDateTime start = LocalDateTime.now();
            MatrixUtil.computeTaskWithBatch(path + "/A-" + position + ".csv", path + "/B.csv", path + "/R-" + position + ".csv");
            System.out.println(Duration.between(start, LocalDateTime.now()).toNanos() + " (ms)");
            //MatrixUtil.computeTaskWithBatch(storage + id.toString(), position);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // deleteTask(id);
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS")) + " [Task Completed] : " + id);
    }

    // delete all the temporary file belongs to the task
    public void deleteTask(UUID id) {
        Path path = Paths.get(storage + id.toString());
        File directoryToBeDeleted = path.toFile();
        try {
            deleteDirectory(directoryToBeDeleted);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
