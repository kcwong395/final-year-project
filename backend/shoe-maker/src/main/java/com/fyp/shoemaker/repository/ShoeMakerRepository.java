package com.fyp.shoemaker.repository;

import com.fyp.shoemaker.model.ShoeMaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ShoeMakerRepository extends JpaRepository<ShoeMaker, UUID> {
}
