package com.security.oauth2.demo;

import com.security.oauth2.demo.config.EnableJDBCAuthorizationServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableJDBCAuthorizationServer
//@EnableAuthorizationServer
@EnableWebSecurity
@EnableResourceServer
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

