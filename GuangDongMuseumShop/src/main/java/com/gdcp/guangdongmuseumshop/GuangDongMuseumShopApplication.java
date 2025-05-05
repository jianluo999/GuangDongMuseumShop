package com.gdcp.guangdongmuseumshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.gdcp.guangdongmuseumshop",
    "com.gdcp.guangdongmuseumshop.mapper"
})
public class GuangDongMuseumShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuangDongMuseumShopApplication.class, args);
    }

}
