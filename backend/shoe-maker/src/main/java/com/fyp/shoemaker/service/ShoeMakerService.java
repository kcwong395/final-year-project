package com.fyp.shoemaker.service;

import com.fyp.shoemaker.model.Elf;
import com.fyp.shoemaker.model.Record;
import com.fyp.shoemaker.model.ShoeMaker;
import com.fyp.shoemaker.repository.RecordRepository;
import com.fyp.shoemaker.repository.ShoeMakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

@Service
public class ShoeMakerService {

    @Autowired
    private ShoeMakerRepository shoeMakerRepository;

    @Autowired
    private RecordRepository recordRepository;

    public void register() {
        try {
            Socket s = new Socket("www.google.com", 80);
            InetAddress smIp = s.getLocalAddress();

            shoeMakerRepository.save(new ShoeMaker(smIp, 8080));
            recordRepository.save(new Record("[ShoeMaker Activated]", smIp.toString(), Record.Type.Normal));

            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Page<ShoeMaker> getShoeMakerWithPage(int page, int size) {
        return shoeMakerRepository.findAll(PageRequest.of(page, size));
    }

}
