package com.fyp.shoemaker.service;

import com.fyp.shoemaker.model.Elf;
import com.fyp.shoemaker.model.Record;
import com.fyp.shoemaker.repository.ElfRepository;
import com.fyp.shoemaker.repository.RecordRepository;
import com.fyp.shoemaker.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElfService {

    @Autowired
    private ElfRepository elfRepository;

    @Autowired
    private RecordRepository recordRepository;

    public List<Elf> getElves() {
        return elfRepository.findAll();
    }

    public void createElf() {
        String[] commands = {
                                "docker",
                                "run",
                                "-d",
                                "--rm",
                                "kcwong395/elf:1.5.3",
                                "/bin/bash",
                                "-c",
                                "service ssh start && java -jar -Xms1g /home/node/fyp/elf-1.jar"
                            };
        try {
            Process p = Runtime.getRuntime().exec(commands);
            p.waitFor();
            recordRepository.save(new Record("[Elf Created]", "Nil", Record.Type.Success));
        }
        catch (IOException | InterruptedException e) {
            recordRepository.save(new Record("[Elf Create Failed]", e.getMessage(), Record.Type.Danger));
        }
    }

    public void deleteElf(UUID elfId) {
        Elf elf = getElf(elfId).get();
        String url = "http:/" + elf.getIp().toString() + ":" + elf.getPort() + "/actuator/shutdown";
        try {
            RequestUtil.initiateRequest("POST", url, "");
            Thread.sleep(3000);
            elf.setStatus(Elf.Status.Disconnected, recordRepository);
            elfRepository.save(elf);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Get elves with a list of uuid
    public List<Elf> getElves(List<UUID> ids) {
        List<Elf> elves = new ArrayList<>();
        for(int i = 0; i < ids.size(); i++) {
            Elf elf = getElf(ids.get(i)).get();
            if (elf.getStatus() == Elf.Status.Active) {
                elves.add(elf);
            }
        }
        return elves;
    }

    public Page<Elf> getElvesWithPage(int page, int size) {
        return elfRepository.findAll(PageRequest.of(page, size));
    }

    public Elf getElfByIp(InetAddress ip) {
        return elfRepository.findByIp(ip);
    }

    public List<Elf> getElvesByStatus(Elf.Status status) {
        return elfRepository.findByStatus(status);
    }

    /*
        This function returns a list contain n active elves
        If there are less than n elves, return null
    */
    public List<UUID> assignElvesToTask(int n) {

        List<Elf> elves = getElvesByStatus(Elf.Status.Active);

        if(elves.size() < n) return null;

        List<UUID> elvesId = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            Elf elf = elves.get(i);
            elf.setStatus(Elf.Status.Busy, recordRepository);
            elfRepository.save(elf);
            elvesId.add(elf.getId());
        }
        return elvesId;
    }

    public void freeElvesFromTask(List<UUID> ids) {
        for(UUID id : ids) {
            Elf elf = getElf(id).get();
            elf.setStatus(Elf.Status.Active, recordRepository);
            elfRepository.save(elf);
        }
    }

    public Optional<Elf> getElf(UUID id){
        return elfRepository.findById(id);
    }

    public Elf register(Elf elf) {

        Elf returnElf = getElfByIp(elf.getIp());

        // if the new registering elf is in the db, treat it as reconnecting the master
        if(returnElf != null) {
            if(returnElf.getStatus().equals(Elf.Status.Shutdown)) {
                reactivate(returnElf.getId());
            }
        }
        else {
            returnElf = elfRepository.save(elf);
            recordRepository.save(new Record("[Elf Register]", returnElf.getIp().toString(), Record.Type.Info, null, returnElf.getId()));
            traceHealthState(returnElf.getId());
        }
        return returnElf;
    }

    public void reactivate(UUID id) {
        Elf elf = getElf(id).get();
        elf.setStatus(Elf.Status.Initialized, recordRepository);
        traceHealthState(elfRepository.save(elf).getId());
        recordRepository.save(new Record("[Elf Reactivated]", elf.getIp().toString(), Record.Type.Warning));
    }

    public void traceHealthState(UUID id) {
        // TODO: Use Threadpool
        Thread t = new Thread(() -> {
            while(true) {
                Elf elf = getElf(id).get();
                boolean isUp = false;
                try {
                    RequestUtil.initiateRequest("GET", "http:/" + elf.getIp().toString() + ":" + elf.getPort() + "/actuator/health", null);
                    isUp = true;
                } catch (Exception e) { }

                elf = getElf(id).get();
                if(isUp) {
                    if(!Elf.Status.Active.equals(elf.getStatus()) && !Elf.Status.Busy.equals(elf.getStatus())) {
                        elf.setStatus(Elf.Status.Active, recordRepository);
                    }
                    elf.setRetry(0);
                }
                else {
                    elf.setStatus(Elf.Status.Disconnected, recordRepository);
                    elf.setRetry(elf.getRetry() + 1);
                }
                elfRepository.save(elf);

                elf = getElf(id).get();
                // if there are three failed trials, the elf is treated as shutdown
                if(elf.getRetry() < 3) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    elf.setRetry(0);
                    elf.setStatus(Elf.Status.Shutdown, recordRepository);
                    elfRepository.save(elf);
                    recordRepository.save(new Record("[Elf Trace Remove]", elf.getIp().toString(), Record.Type.Danger, null, elf.getId()));
                    break;
                }
            }
        });
        t.start();
    }
}
