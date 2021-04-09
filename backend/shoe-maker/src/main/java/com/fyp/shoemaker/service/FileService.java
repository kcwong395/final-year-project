package com.fyp.shoemaker.service;

import com.fyp.shoemaker.model.Document;
import com.fyp.shoemaker.model.Elf;
import com.fyp.shoemaker.model.Record;
import com.fyp.shoemaker.model.Task;
import com.fyp.shoemaker.repository.FileRepository;
import com.fyp.shoemaker.repository.RecordRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.print.Doc;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory;

@Service
public class FileService {

    @Value("${storagePrefix}")
    private String storage;

    @Value("${elfPass}")
    private String elfPass;

    @Value("${os}")
    private String os;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TaskService taskService;

    public Document addFile(Document file) {
        return fileRepository.save(file);
    }

    /*
        upload the file to a path
        return null if upload failed
     */
    public Document uploadFile(MultipartFile file, UUID taskId, Character position) {
        try {
            // Create dir to store the file, and copy the content to a new csv file
            createDir(storage + taskId.toString());
            Path copyLocation = Paths.get(storage + taskId.toString() + '/' + position + ".csv");
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

            // Store the file record to the db
            Document document = addFile(new Document(taskId, storage, file.getOriginalFilename(), position));
            recordRepository.save(new Record("[File Uploaded]", document.getFileName(), Record.Type.Info, taskId, null));

            return document;
        }
        catch (Exception e) {
            recordRepository.save(new Record("[File Upload Fail]", taskId + ": " + position, Record.Type.Danger, taskId, null));
            e.printStackTrace();
        }
        return null;
    }

    public Pair<String, byte[]> downloadFile(UUID taskId, char position) throws IOException {
        Document document = fileRepository.findByTaskIdAndPosition(taskId, position);
        return new ImmutablePair<>(document.getFileName(), Files.readAllBytes(Paths.get(document.getFileUrl())));
    }

    public void deleteFile(UUID taskId, char position) {
        Path path = Paths.get(storage + taskId.toString() + "/" + position + ".csv");
        deleteFiles(new File[]{ path.toFile() });
        fileRepository.delete(fileRepository.findByTaskIdAndPosition(taskId, position));
        recordRepository.save(new Record("[File Removal]", path.toString(), Record.Type.Success, taskId, null));
    }

    public void deleteFilesUnderTask(UUID taskId) {
        Path path = Paths.get(storage + taskId.toString());
        deleteFiles(path.toFile().listFiles());
        deleteFiles(new File[]{ path.toFile() });
    }

    // delete all the temporary file belongs to the task
    public void deleteFiles(File[] files) {
        for(File f : files) {
            f.delete();
        }
    }

    public void fileTransfer(String src, String dest) {
        try {
            String command;
            if(os.equals("Windows")) {
                command = "cmd.exe /c echo n | pscp -pw " + elfPass + " -P 22 " + src + " " + dest;
            }
            else {
                command = "sshpass -p " + elfPass + " scp " + src + " " + dest;
            }

            Process p = Runtime.getRuntime().exec(command);
            //System.out.println(command);

            p.waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Document> getFilesUnderTask(UUID taskId) {
        return fileRepository.findByTaskId(taskId);
    }

    public void createDir(String pathStr) {
        try {
            Files.createDirectory(Path.of(pathStr));
        }
        catch (IOException ignored) {

        }
    }
}
