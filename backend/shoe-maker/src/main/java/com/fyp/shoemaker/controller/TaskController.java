package com.fyp.shoemaker.controller;

import com.fyp.shoemaker.model.Task;
import com.fyp.shoemaker.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{page}/{size}")
    public Page<Task> getTasksWithPage(@PathVariable("page") Integer page,
                                       @PathVariable("size") Integer size) {
        return taskService.getTasksWithPage(page - 1, size);
    }

    @GetMapping("/{id}")
    public Optional<Task> getTask(@PathVariable final UUID id) {
        return taskService.getTask(id);
    }

    @PostMapping
    public Task submitTask(@RequestBody Task task) {
        return taskService.submitTask(task);
    }

    @PostMapping("pause/{id}")
    public void pauseTask(@PathVariable UUID id) {
        taskService.pauseTask(id);
    }

    @PostMapping("cancel/{id}")
    public void cancelTask(@PathVariable UUID id) {
        taskService.cancelTask(id);
    }
}
