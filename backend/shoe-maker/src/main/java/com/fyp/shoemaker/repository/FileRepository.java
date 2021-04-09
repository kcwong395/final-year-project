package com.fyp.shoemaker.repository;

import com.fyp.shoemaker.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<Document, UUID> {
    List<Document> findByTaskId(UUID taskId);

    Document findByTaskIdAndPosition(UUID taskId, char position);
}
