package com.jemigraph.jemigraph_backend.services;
import com.jemigraph.jemigraph_backend.DTO.TeamCreateDTO;
import com.jemigraph.jemigraph_backend.DTO.TeamResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Team;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamService {
    List<TeamResponseDTO> getAllTeamMembers();
    Optional<TeamResponseDTO> getTeamMemberById(UUID id);
    TeamResponseDTO saveTeamMember(TeamCreateDTO team, MultipartFile file);
    void deleteTeamMember(UUID UUId);
}