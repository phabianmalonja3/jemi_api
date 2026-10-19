package com.jemigraph.jemigraph_backend.schedulers;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PhotographerRotationScheduler {

  private final UserRepository userRepository;

  private volatile List<User> rotatedPhotographers = List.of();

  private int rotationCount = 0;

  public PhotographerRotationScheduler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostConstruct
  public void initialize() {
    refreshAndRotate();
  }

  // Run every 5 minutes
  @Scheduled(fixedRate = 300000, initialDelay = 300000)
  public synchronized void rotatePhotographers() {
    refreshAndRotate();
  }

  private synchronized void refreshAndRotate() {

    List<User> photographers =
        new ArrayList<>(userRepository.findEligiblePhotographersForRotation(UserRole.PHOTOGRAPHER));

    if (photographers.isEmpty()) {
      rotatedPhotographers = List.of();
      rotationCount = 0;
      return;
    }

    int shift = rotationCount % photographers.size();

    Collections.rotate(photographers, -shift);

    rotatedPhotographers = List.copyOf(photographers);

    rotationCount++;
  }

  public List<User> getRotatedPhotographers() {
    return rotatedPhotographers;
  }
}
