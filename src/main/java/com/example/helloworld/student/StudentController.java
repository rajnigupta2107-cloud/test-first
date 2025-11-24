package com.example.helloworld.student;

import com.example.helloworld.messaging.StudentCreatedEvent;
import com.example.helloworld.messaging.StudentEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.net.URI;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository repository;
    private final StudentEventPublisher eventPublisher;

    public StudentController(StudentRepository repository, StudentEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    // Create
    @PostMapping
    public ResponseEntity<Student> create(@RequestBody Student input) {
        if (input.getId() != null) {
            // Do not allow client-specified IDs on create
            input.setId(null);
        }
        if (input.getEmail() != null && repository.existsByEmail(input.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        Student saved = repository.save(input);
        // Publish event (no-op if Kafka is disabled)
        try {
            eventPublisher.publishStudentCreated(new StudentCreatedEvent(saved.getId(), saved.getName(), saved.getEmail()));
        } catch (Exception e) {
            // Do not fail the request if event publishing fails
        }
        return ResponseEntity.created(URI.create("/students/" + saved.getId())).body(saved);
    }

    // List all
    @GetMapping
    public java.util.List<Student> list() {
        return repository.findAll();
    }

    // Read by ID
    @GetMapping("/{id}")
    public Student getOne(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    // Update by ID
    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @RequestBody Student update) {
        return repository.findById(id).map(existing -> {
            if (update.getName() != null) existing.setName(update.getName());
            if (update.getEmail() != null) {
                // If email is changing, ensure uniqueness
                if (!update.getEmail().equals(existing.getEmail()) && repository.existsByEmail(update.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                }
                existing.setEmail(update.getEmail());
            }
            return repository.save(existing);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    // Delete by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
