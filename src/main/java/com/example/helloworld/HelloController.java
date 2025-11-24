package com.example.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // Moved from "/" to "/hello" so that static index.html is served at root
    @GetMapping(path = "/hello", produces = "text/plain;charset=UTF-8")
    public String hello() {
        return "hello world for new application!!";
    }

    // Lightweight liveness probe to help troubleshoot "empty reply" issues
    @GetMapping(path = "/health", produces = "text/plain;charset=UTF-8")
    public String health() {
        return "OK";
    }
}
