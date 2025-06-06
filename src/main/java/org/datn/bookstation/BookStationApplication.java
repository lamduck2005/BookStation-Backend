package org.datn.bookstation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookStationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookStationApplication.class, args);
        System.out.println("BookStation Application is running...");
    }

}
