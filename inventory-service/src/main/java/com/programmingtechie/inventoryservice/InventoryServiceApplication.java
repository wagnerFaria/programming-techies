package com.programmingtechie.inventoryservice;

import com.programmingtechie.inventoryservice.model.Inventory;
import com.programmingtechie.inventoryservice.repositoy.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
//        return args -> {
//            Inventory iphone_13 = Inventory
//                    .builder()
//                    .skuCode("iphone_13")
//                    .quantity(100)
//                    .build();
//            Inventory iphone_13_red = Inventory
//                    .builder()
//                    .skuCode("iphone_13_red")
//                    .quantity(0)
//                    .build();
//
//            inventoryRepository.save(iphone_13);
//            inventoryRepository.save(iphone_13_red);
//
//        };
//    }
}
