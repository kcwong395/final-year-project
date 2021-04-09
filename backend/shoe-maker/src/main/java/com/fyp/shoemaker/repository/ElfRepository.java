package com.fyp.shoemaker.repository;

import com.fyp.shoemaker.model.Elf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

@Repository
public interface ElfRepository extends JpaRepository<Elf, UUID> {

    Elf findByIp(InetAddress ip);

    List<Elf> findByStatus(Elf.Status status);
}