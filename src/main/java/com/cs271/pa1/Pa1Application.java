package com.cs271.pa1;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.cs271.pa1.dto.ClientPorts;
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
        
        userInterface.start(clientName, getClientPorts(clientName));
    }

    private static List<Integer> getClientPorts(String clientName) {
        return switch(clientName) {
            case "A" -> List.of(ClientPorts.B.getPort(), ClientPorts.C.getPort());
            case "B" -> List.of(ClientPorts.A.getPort(), ClientPorts.C.getPort());
            case "C" -> List.of(ClientPorts.A.getPort(), ClientPorts.B.getPort());
            default -> throw new IllegalArgumentException("Invalid client name");
        };
    }
}