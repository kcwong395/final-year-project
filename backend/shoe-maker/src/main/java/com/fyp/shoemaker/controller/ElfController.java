package com.fyp.shoemaker.controller;

import com.fyp.shoemaker.model.Elf;
import com.fyp.shoemaker.service.ElfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/elf")
public class ElfController {

    @Autowired
    private ElfService elfService;

    @GetMapping
    public List<Elf> getElves() {
        return elfService.getElves();
    }

    @GetMapping("/{page}/{size}")
    public Page<Elf> getElvesWithPage(@PathVariable("page") Integer page,
                                      @PathVariable("size") Integer size) {
        return elfService.getElvesWithPage(page - 1, size);
    }

    @GetMapping("/state/{state}")
    public List<Elf> getElvesByStatus(@PathVariable final Elf.Status status) {
        return elfService.getElvesByStatus(status);
    }

    @GetMapping("/{id}")
    public Optional<Elf> getElf(@PathVariable final UUID id){
        return elfService.getElf(id);
    }

    @PostMapping
    public Elf registerElf(@RequestBody Elf elf) {
        return elfService.register(elf);
    }

    @PostMapping("/create")
    public void createElf() {
        elfService.createElf();
    }

    @PostMapping("/delete/{elfId}")
    public void deleteElf(@PathVariable final UUID elfId) {
        elfService.deleteElf(elfId);
    }

}
