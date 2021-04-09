package com.fyp.elf.controller;

import com.fyp.elf.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("api/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // This let the elf setup the folder for storing the matrix csv
    @PostMapping("{id}")
    public void createTask(@PathVariable UUID id) {
        taskService.createTask(id);
    }

    // This will start the computation job
    @PostMapping("{id}/{position}")
    public void receiveTask(@PathVariable UUID id,
                            @PathVariable int position) {
        taskService.processTask(id, position);
    }
}
