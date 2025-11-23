package com.example.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // Root endpoint returns a simple plain text body
    @GetMapping(path = "/", produces = "text/plain;charset=UTF-8")
    public String hello() {
        return "hello world!!";
    }

    // Lightweight liveness probe to help troubleshoot "empty reply" issues
    @GetMapping(path = "/health", produces = "text/plain;charset=UTF-8")
    public String health() {
        return "OK";
    }
}
