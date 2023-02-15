package cse326.SoftwareEng.database;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootLoginApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLoginApp.class, args);
        System.out.println("Hello World! Server is running.");
    }

}