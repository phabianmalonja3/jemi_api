package com.jemigraph.jemigraph_backend.controllers;
import com.jemigraph.jemigraph_backend.DTO.LocationResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.LocationRequestDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private  final LocationService locationService;
    private  final UserRepository userRepository;
    @PostMapping("/update")
    public ResponseEntity<LocationResponseDTO> updateLocation(
            @Valid @RequestBody LocationRequestDTO locationRequestDto,
            Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(locationService.updateUserLocation(user, locationRequestDto));
    }
    @GetMapping("/get")
    public ResponseEntity<LocationResponseDTO> getUserLocation(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(locationService.getUserLocation(user));
    }
}

