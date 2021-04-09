package com.fyp.shoemaker.model;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Data
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String time;

    private Type type;

    private Category category;

    @NonNull
    private String header;

    @NonNull
    @Column(length = 800)
    private String content;

    private UUID taskId;

    private UUID elfId;

    public Record(String header, String content, Type type) {
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
        this.header = header;
        this.content = content;
        this.type = type;
        this.category = Category.System;
        System.out.println(this.toString());
    }

    public Record(String header, String content, Type type, UUID taskId, UUID elfId) {
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
        this.header = header;
        this.content = content;
        this.type = type;
        this.taskId = taskId;
        this.category = (taskId == null ? Category.System : Category.Task);
        this.elfId = elfId;
        System.out.println(this.toString());
    }

    public Record() {

    }

    public enum Type {
        Normal, Success, Warning, Danger, Info
    }

    public enum Category {
        System, Task
    }
}
