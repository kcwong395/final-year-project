package com.fyp.shoemaker;

import com.fyp.shoemaker.model.ShoeMaker;
import com.fyp.shoemaker.service.ShoeMakerService;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.io.*;
import java.net.Socket;

@EnableOpenApi // http://localhost:8080/swagger-ui/index.html
@SpringBootApplication
public class ShoeMakerApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ShoeMakerApplication.class, args);

        ShoeMakerService shoeMakerService = applicationContext.getBean(ShoeMakerService.class);
        shoeMakerService.register();

    }

}
