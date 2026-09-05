package com.jemigraph.jemigraph_backend.controllers;


import com.jemigraph.jemigraph_backend.DTO.TeamCreateDTO;
import com.jemigraph.jemigraph_backend.DTO.TeamResponseDTO;
import com.jemigraph.jemigraph_backend.services.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamResponseDTO>> getAllTeamMembers() {
        List<TeamResponseDTO> members = teamService.getAllTeamMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> getTeamMemberById(@PathVariable UUID id) {
        return teamService.getTeamMemberById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeamResponseDTO> createTeamMember(
            @Valid @RequestPart("data") TeamCreateDTO createDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        TeamResponseDTO createdMember = teamService.saveTeamMember(createDTO, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeamMember(@PathVariable UUID id) {
        teamService.deleteTeamMember(id);
        return ResponseEntity.noContent().build();
    }
}