package com.example.diet.food;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 食物服务应用启动类
 */
@SpringBootApplication
@EnableDubbo
@EnableCaching
@MapperScan("com.example.diet.food.infrastructure.persistence.mapper")
public class FoodServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodServiceApplication.class, args);
    }
}
