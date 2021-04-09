package com.fyp.shoemaker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fyp.shoemaker.model.Record;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    Page<Record> findByCategory(Record.Category category, Pageable pageable);

    List<Record> findByTaskId(UUID taskId);

    List<Record> findByElfId(UUID elfId);

    List<Record> findByTaskIdAndElfId(UUID taskId, UUID elfId);

    Page<Record> findByTaskId(UUID taskId, Pageable pageable);

    Page<Record> findByElfId(UUID elfId, Pageable pageable);
}