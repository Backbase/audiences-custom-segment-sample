package com.backbase.sample.segmentation;

import org.springframework.boot.SpringApplication;

public class LocalCustomSegmentApplication {

    public static void main(String[] args) {
        SpringApplication.from(CustomSegmentApplication::main).with(TestcontainersConfiguration.class).run(args);
    }


}
