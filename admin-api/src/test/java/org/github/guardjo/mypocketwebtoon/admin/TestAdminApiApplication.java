package org.github.guardjo.mypocketwebtoon.admin;

import org.springframework.boot.SpringApplication;

public class TestAdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(AdminApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
