package com.fyp.shoemaker.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Document {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private UUID taskId;
    private String fileName;
    private Character position;
    private LocalDateTime uploadTime;
    private String fileUrl;
    private Long fileSize;

    public Document() {}

    public Document(UUID taskId, String storage, String fileName, Character position) {
        this.fileName = fileName;
        this.taskId = taskId;
        this.position = position;
        this.uploadTime = LocalDateTime.now();
        this.fileUrl = storage + taskId.toString() + "/" + position + ".csv";
        this.fileSize = new File(this.fileUrl).length();
    }
}
