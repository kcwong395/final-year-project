package com.fyp.shoemaker.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Data
public class Task {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private LocalDateTime createTime;

    private LocalDateTime endTime;

    // in sec
    private Long duration;

    @NonNull
    private Application application;

    @NonNull
    private Algorithm algorithm;

    @NonNull
    private Approach approach;

    private int batch;

    private int totalBatch;

    @ElementCollection(fetch=FetchType.EAGER)
    private List<UUID> elvesOnDuty;

    private Status status;

    // Calculate all overhead in ms (10^-3)
    @ElementCollection
    private Map<String, Long> timeMap;

    private int n;

    private int k;

    public Task() {
        createTime = LocalDateTime.now();
        batch = 0;
        timeMap = new ConcurrentHashMap<>();
    }

    public int getN() {
        if(approach.equals(Approach.NormApp)) {
            return 1;
        }
        else if(approach.equals(Approach.DistApp)) {
            return this.n;
        }
        return Integer.parseInt(algorithm.toString().split("_")[1]);
    }

    public int getK() {
        if(approach.equals(Approach.CodeApp)) {
            return Integer.parseInt(algorithm.toString().split("_")[2]);
        }
        return 0;
    }

    // Created: task is setup
    // Started: the computation has begun
    public enum Status {
        Created, Started, Error, Paused, Canceled, Completed
    }

    public enum Application {
        MatrixMulti, LinearReg
    }

    public enum Algorithm {
        NA, RSCode_5_3
    }

    public enum Approach {
        NormApp, DistApp, CodeApp
    }
}
