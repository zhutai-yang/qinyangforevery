package com.tt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tt")
@MapperScan("com.tt.infrastructure.persistence.mapper")
public class TtAdminApplication {

  public static void main(String[] args) {
    SpringApplication.run(TtAdminApplication.class, args);
  }
}
