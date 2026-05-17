package com.gcsc.studentcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudentCenterApplication {

  public static void main(String[] args) {
    SpringApplication.run(StudentCenterApplication.class, args);
  }

  @Bean
  ApplicationRunner printBanner() {
    return args -> {
      var banner = """
                        ____  ____    _    ___
                       | __ )|  _ \\  / \\  |_ _|
                       |  _ \\| | | |/ _ \\  | |
                       | |_) | |_| / ___ \\ | |
                       |____/|____/_/   \\_\\___|
          """;
      System.out.println(banner);
    };
  }
}
