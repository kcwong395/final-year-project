package com.fyp.shoemaker.controller;

import com.fyp.shoemaker.model.Record;
import com.fyp.shoemaker.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/log")
public class RecordController {

    @Autowired
    private RecordRepository recordRepository;

    @GetMapping("/{category}/{page}/{size}")
    public Page<Record> getRecordWithPage(@PathVariable("category") Record.Category category,
                                          @PathVariable("page") Integer page,
                                          @PathVariable("size") Integer size) {
        return recordRepository.findByCategory(category, PageRequest.of(page - 1, size, Sort.by("time").descending()));
    }

    @GetMapping("task/{taskId}/{page}/{size}")
    public Page<Record> getTaskRecordWithPage(@PathVariable("page") Integer page,
                                          @PathVariable("size") Integer size,
                                          @PathVariable("taskId") UUID taskId) {
        return recordRepository.findByTaskId(taskId, PageRequest.of(page - 1, size, Sort.by("time").descending()));
    }

    @GetMapping("elf/{elfId}/{page}/{size}")
    public Page<Record> getElfRecordWithPage(@PathVariable("page") Integer page,
                                          @PathVariable("size") Integer size,
                                          @PathVariable("elfId") UUID elfId) {
        return recordRepository.findByElfId(elfId, PageRequest.of(page - 1, size, Sort.by("time").descending()));
    }

    @GetMapping(params = "taskId")
    public List<Record> getRecordByTaskId(@RequestParam("taskId") UUID taskId) {
        return recordRepository.findByTaskId(taskId);
    }

    @GetMapping(params = "elfId")
    public List<Record> getRecordByElfId(@RequestParam("elfId") UUID elfId) {
        return recordRepository.findByElfId(elfId);
    }

    @PostMapping
    public Record addRecord(@RequestBody Record record) {
        return recordRepository.save(record);
    }
}
