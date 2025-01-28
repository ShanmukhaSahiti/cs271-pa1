package com.cs271.pa1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.cs271.pa1.ui.ClientUserInterface;

@SpringBootApplication
public class Pa1Application {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar app.jar <ClientName>");
            System.exit(1);
        }

        String clientName = args[0];

        ConfigurableApplicationContext context = SpringApplication.run(Pa1Application.class, args);
        ClientUserInterface userInterface = context.getBean(ClientUserInterface.class);
        
        userInterface.start(clientName);
    }

    
}