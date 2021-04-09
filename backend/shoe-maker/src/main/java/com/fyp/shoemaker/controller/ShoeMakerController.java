package com.fyp.shoemaker.controller;

import com.fyp.shoemaker.model.Elf;
import com.fyp.shoemaker.model.ShoeMaker;
import com.fyp.shoemaker.service.ShoeMakerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/sm")
public class ShoeMakerController {

    @Autowired
    private ShoeMakerService shoeMakerService;

    @GetMapping("/{page}/{size}")
    public Page<ShoeMaker> getShoeMakerWithPage(@PathVariable("page") Integer page,
                                                @PathVariable("size") Integer size) {
        return shoeMakerService.getShoeMakerWithPage(page - 1, size);
    }
}
