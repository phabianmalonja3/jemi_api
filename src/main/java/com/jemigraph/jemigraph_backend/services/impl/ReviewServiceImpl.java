package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.ReviewDTO;
import com.jemigraph.jemigraph_backend.DTO.ReviewResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import com.jemigraph.jemigraph_backend.events.Review;
import com.jemigraph.jemigraph_backend.mappers.ReviewMapper;
import com.jemigraph.jemigraph_backend.repositories.BookingRepository;
import com.jemigraph.jemigraph_backend.repositories.ReviewRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ReviewMapper reviewMapper;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(UUID bookingId, ReviewDTO request, String client) {
        var user =userRepository.findByEmail(client)
                .orElseThrow(() -> new RuntimeException("User  Not Found!"));



        Bookings booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking Not Found!"));


        if (reviewRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new RuntimeException("You Have ready give us your over view!");
        }

        if (!booking.getClient().getId().equals(user.getId())) {
            throw new RuntimeException("you can create someone booking!");
        }
        if (!booking.getStatus().equals(BookingStatus.COMPLETED)) {
            throw new RuntimeException("you can rate only the only Completed one !");
        }

        Review review = Review.builder()
                .booking(booking)
                .photographer(booking.getPhotographer())
                .client(user)
                .rating(request.getRating())
                .comment(request.getComment())
//                .createdAt(LocalDateTime.now())
                .build();
            var reviewCreated =    reviewMapper.toResponseDTO(reviewRepository.save(review));
        User photographer = review.getPhotographer();
        double currentAvg = (photographer.getAverageRating() != null) ? photographer.getAverageRating() : 0.0;
        long totalReviews = (photographer.getTotalReviews() != null) ? photographer.getTotalReviews() : 0;
        double newAverage = ((currentAvg * totalReviews) + request.getRating()) / (totalReviews + 1);

        photographer.setAverageRating(newAverage);
        photographer.setTotalReviews(totalReviews + 1);

        userRepository.save(photographer);


        return reviewCreated;

    }

    @Override
    @Transactional
    public List<ReviewResponseDTO> findByPhotographerId(UUID photographerId) {
        List<Review> reviews = reviewRepository.findByPhotographerId(photographerId);

        return reviewMapper.toResponseDTOList(reviews);
    }

    @Override
    public List<ReviewResponseDTO> findAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

}
