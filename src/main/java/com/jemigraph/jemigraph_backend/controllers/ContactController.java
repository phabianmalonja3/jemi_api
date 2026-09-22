package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.ContactRequest;
import com.jemigraph.jemigraph_backend.services.EmailService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/contact")
public class ContactController {

  private final EmailService emailService;

  @PostMapping
  public ResponseEntity<?> submitContact(@RequestBody ContactRequest request) {

    emailService.sendContactFormReceived(
        request.getEmail(), request.getName(), request.getSubject(), request.getMessage());

    return ResponseEntity.ok(Map.of("message", "Message received"));
  }
}
