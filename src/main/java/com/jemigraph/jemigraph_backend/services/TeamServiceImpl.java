package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.TeamCreateDTO;
import com.jemigraph.jemigraph_backend.DTO.TeamResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Team;
import com.jemigraph.jemigraph_backend.mappers.TeamMapper;
import com.jemigraph.jemigraph_backend.repositories.TeamRepository;
import com.jemigraph.jemigraph_backend.services.impl.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final FileStorageService fileStorageService; // Inject your file storage service here

    @Override
    public List<TeamResponseDTO> getAllTeamMembers() {
        return teamRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(teamMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TeamResponseDTO> getTeamMemberById(UUID id) {
        return teamRepository.findById(id)
                .map(teamMapper::toResponseDto);
    }

    @Override
    public TeamResponseDTO saveTeamMember(TeamCreateDTO createDto, MultipartFile file) {
        // 1. Map DTO to Entity
        Team team = teamMapper.toEntity(createDto);

        // 2. Validate and store the image file if present
        if (file != null && !file.isEmpty()) {
            // Optional validation (size, content type)
            long maxFileSize = 2 * 1024 * 1024; // 2MB
            if (file.getSize() > maxFileSize) {
                throw new IllegalArgumentException("File size exceeds the maximum allowed limit of 2MB.");
            }

            List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "image/webp");
            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType)) {
                throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP images are allowed.");
            }

            // Store the file and get the relative path
            String imagePath = fileStorageService.storeTeamImage(file);

            // Set the image URL/path on the entity
            team.setImageUrl(imagePath);
        }

        // 3. Save to database
        Team savedTeam = teamRepository.save(team);

        // 4. Map to response DTO and return
        return teamMapper.toResponseDto(savedTeam);
    }

    @Override
    public void deleteTeamMember(UUID id) {
        teamRepository.deleteById(id);
    }
}