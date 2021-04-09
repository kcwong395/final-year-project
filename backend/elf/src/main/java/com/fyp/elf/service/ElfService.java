package com.fyp.elf.service;

import java.lang.reflect.Type;
import java.net.*;

import com.fyp.elf.util.RequestUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Service
public class ElfService {


    @Value("${storagePrefix}")
    private String storage;

    @Value("${shoemaker.ip}")
    private String shoemakerIp;
    @Value("${shoemaker.port}")
    private String shoemakerPort;

    @Value("${server.port}")
    private String port;

    public void register() {
        while(true) {
            try {

                Socket s = new Socket("www.google.com", 80);
                String elfIp = s.getLocalAddress().getHostAddress();
                s.close();

                String elfUrl = "http://" + shoemakerIp + ":" + shoemakerPort + "/api/elf";

                HashMap<String, String> elfBody = new HashMap<>();
                elfBody.put("ip", elfIp);
                elfBody.put("port", port);

                String response = RequestUtil.initiateRequest("POST", elfUrl, new Gson().toJson(elfBody));

                Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                HashMap<String, String> responseMap = new Gson().fromJson(response, type);

                if (responseMap.containsKey("id")) {
                    System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS")) + " [Elf Registration] : Succeed -> " + responseMap.toString());
                    break;
                }
            } catch (Exception e) {
                System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS")) + " [Elf Registration] : Connection to Server Failed");
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
