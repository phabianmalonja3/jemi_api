package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.PhotographerProfileDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.mappers.PhotographerMapper;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PhotographerRotationDataService {

  private final UserRepository userRepository;
  private final PhotographerMapper photographerMapper;

  public PhotographerRotationDataService(
      UserRepository userRepository, PhotographerMapper photographerMapper) {
    this.userRepository = userRepository;
    this.photographerMapper = photographerMapper;
  }

  @Transactional(readOnly = true)
  public List<PhotographerProfileDTO> loadEligiblePhotographers() {
    List<User> photographers =
        userRepository.findEligiblePhotographersForRotation(UserRole.PHOTOGRAPHER);

    return photographers.stream().map(photographerMapper::toDto).toList();
  }
}
