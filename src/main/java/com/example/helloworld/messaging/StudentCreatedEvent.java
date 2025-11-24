package com.example.helloworld.messaging;

import java.time.Instant;

public class StudentCreatedEvent {
    private Long id;
    private String name;
    private String email;
    private Instant createdAt = Instant.now();

    public StudentCreatedEvent() {}

    public StudentCreatedEvent(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
