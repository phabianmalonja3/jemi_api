package com.jemigraph.jemigraph_backend.schedulers;

import com.jemigraph.jemigraph_backend.DTO.PhotographerProfileDTO;
import com.jemigraph.jemigraph_backend.services.PhotographerRotationDataService;
import java.util.Collections;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PhotographerRotationScheduler {

  private final PhotographerRotationDataService rotationDataService;

  private volatile List<PhotographerProfileDTO> rotatedPhotographers = List.of();

  private int rotationCount = 0;

  public PhotographerRotationScheduler(PhotographerRotationDataService rotationDataService) {
    this.rotationDataService = rotationDataService;
  }

  // Run immediately, then every 5 minutes.
  @Scheduled(fixedRate = 300000, initialDelay = 0)
  public synchronized void rotatePhotographers() {
    refreshAndRotate();
  }

  private synchronized void refreshAndRotate() {
    try {
      List<PhotographerProfileDTO> photographers = rotationDataService.loadEligiblePhotographers();

      if (photographers == null || photographers.isEmpty()) {
        rotatedPhotographers = List.of();
        rotationCount = 0;
        return;
      }

      // Rotate the list by one position every 5 minutes.
      int shift = rotationCount % photographers.size();

      Collections.rotate(photographers, -shift);

      rotatedPhotographers = List.copyOf(photographers);
      rotationCount++;

    } catch (Exception e) {
      // Keep the last successful list if refreshing fails.
      System.err.println("Failed to rotate photographers: " + e.getMessage());
    }
  }

  public List<PhotographerProfileDTO> getRotatedPhotographers() {
    return rotatedPhotographers;
  }
}
