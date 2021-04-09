package com.fyp.shoemaker.controller;

import com.fyp.shoemaker.model.Document;
import com.fyp.shoemaker.service.FileService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Paths;

@RestController
@RequestMapping("api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/{taskId}")
    public List<Document> getFilesUnderTask(@PathVariable final UUID taskId) {
        return fileService.getFilesUnderTask(taskId);
    }

    @GetMapping("/downloads/{id}/{position}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("id") UUID id,
                                                 @PathVariable("position") char position) throws IOException {
        Pair<String, byte[]> p = fileService.downloadFile(id, position);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData(p.getLeft(), p.getLeft());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(p.getRight(), headers, HttpStatus.OK);
    }

    @DeleteMapping("/{taskId}/{position}")
    public void deleteFile(@PathVariable("taskId") UUID taskId, @PathVariable("position") char position) {
        fileService.deleteFile(taskId, position);
    }

    @DeleteMapping("/{taskId}")
    public void deleteFile(@PathVariable("taskId") UUID taskId) {
        fileService.deleteFilesUnderTask(taskId);
    }

    @PostMapping("/uploads")
    public Document uploadFile(@RequestParam("file") MultipartFile file,
                               @RequestParam("taskId") UUID taskId,
                               @RequestParam("position") Character position) {
        return fileService.uploadFile(file, taskId, position);
    }
}
