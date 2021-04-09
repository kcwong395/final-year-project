package com.fyp.shoemaker.model;

import com.fyp.shoemaker.repository.RecordRepository;
import com.github.javafaker.Faker;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Entity
@Data
public class Elf implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(unique = true)
    private InetAddress ip;

    private String name;

    @NonNull
    private int port;

    private Status status;

    private int retry;

    public Elf() {
        name = new Faker(new Locale("en-GB")).name().fullName();
        status = Status.Initialized;
        retry = 0;
    }

    public void setStatus(Elf.Status status, RecordRepository recordRepository) {
        if(this.status != status) {
            recordRepository.save(new Record("[Elf State Change]", this.status.toString() + " -> " + status, Record.Type.Warning, null, this.getId()));
            this.status = status;
        }
    }

    // A newly register elf is initialized
    // After passing the healthcheck conducted by shoemaker, it becomes Active
    // Busy when ram usage is higher than threshold
    // Disconnected means fail to connect
    // If shoemaker is shutdown, it fails to connect for 3 times and no longer trace it
    public enum Status {
        Initialized, Active, Busy, Disconnected, Shutdown
    }
}