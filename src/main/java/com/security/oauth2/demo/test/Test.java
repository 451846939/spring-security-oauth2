package com.security.oauth2.demo.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {

    @GetMapping("test")
    public String test(){
        return "test";
    }
}
