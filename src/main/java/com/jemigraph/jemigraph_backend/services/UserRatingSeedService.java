package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRatingSeedService {

    private final UserRepository userRepository;

    @Transactional
    public void seedDefaultUserRatings() {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getAverageRating() == null)
                .toList();
        for (User user : users) {
            user.setAverageRating(0.0);
            user.setTotalReviews(0L);
            userRepository.save(user);
        }

        if (!users.isEmpty()) {
            System.out.println("Rating Seeding complete: " + users.size() + " users updated with 0.0 rating.");
        }
    }
}