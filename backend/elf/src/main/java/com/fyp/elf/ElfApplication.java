package com.fyp.elf;

import com.fyp.elf.service.ElfService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

// health check point: http://ip:port/actuator/health
@SpringBootApplication
public class ElfApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ElfApplication.class, args);

        ElfService elfService = applicationContext.getBean(ElfService.class);
        elfService.register();
    }

}
