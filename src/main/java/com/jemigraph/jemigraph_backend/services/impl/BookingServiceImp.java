package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.*;
import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import com.jemigraph.jemigraph_backend.enums.BookingType;
import com.jemigraph.jemigraph_backend.enums.PaymentStatus;
import com.jemigraph.jemigraph_backend.events.BookingCreatedEvent;
import com.jemigraph.jemigraph_backend.events.BookingStatusChangedEvent;
import com.jemigraph.jemigraph_backend.mappers.BookingMapper;
import com.jemigraph.jemigraph_backend.repositories.BookingRepository;
import com.jemigraph.jemigraph_backend.repositories.PkgRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.BookingService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImp implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PkgRepository pkgRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BookingMapper bookingMapper;

    @Override
    public List<BookingDTO> getBookingsByPhotographerEmail(String email) {
        return bookingRepository.findByPhotographerEmail(email).stream().map(
                bookingMapper::toDto
        ).toList();
    }

    @Transactional
    @Override
    public BookingDTO saveBooking(BookingDTO dto, String email) {
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client account not found."));
        User photographer = userRepository.findById(dto.getPhotographerId())
                .orElseThrow(() -> new RuntimeException("Photographer not found."));
        Pkg pkg = pkgRepository.findById(dto.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found."));
        Bookings booking = Bookings.builder()
                .client(client)
                .photographer(photographer)
                .pkg(pkg)
                .type(BookingType.valueOf(dto.getType()))
                .pickupTime(dto.getPickupTime())
                .addressName(dto.getAddressName())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .status(BookingStatus.PENDING)
                .build();
        Bookings savedBooking = bookingRepository.save(booking);
        String photographerPhone = (photographer.getUserProfile() != null) ? photographer.getUserProfile().getPhone() : null;
        String clientAvatar = client.getProfileImageUrl();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedPickupTime = (savedBooking.getPickupTime() != null)
                ? savedBooking.getPickupTime().format(formatter)
                : "N/A";
        eventPublisher.publishEvent(new BookingCreatedEvent(
                savedBooking.getId(),
                savedBooking.getPhotographer().getId(),
                client.getName(),
                photographer.getFcmToken(),
                photographerPhone,
                clientAvatar,
                formattedPickupTime,
                booking.getAddressName(),
                booking.getStatus().name()
        ));
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public Bookings updateBookingStatus(UUID bookingId, BookingStatus newStatus) {
        Bookings booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
        booking.setStatus(newStatus);
        Bookings updatedBooking = bookingRepository.save(booking);
        String clientFcmToken = booking.getClient().getFcmToken();
        String photographerName = booking.getPhotographer().getName();
        String clientPhone = booking.getClient().getUserProfile() != null
                ? booking.getClient().getUserProfile().getPhone() : null;
        eventPublisher.publishEvent(new BookingStatusChangedEvent(
                updatedBooking.getId(),
                updatedBooking.getClient().getId(),
                newStatus,
                clientFcmToken,
                clientPhone,
                photographerName,
                 booking.getPhotographer().getUserProfile().getProfileImage()
        ));
        return updatedBooking;
    }
    @Override
    public Optional<Bookings> getActiveBookingForClient(String email) {
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Client not found"));
        List<BookingStatus> finishedStatuses = List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED);
        log.info(finishedStatuses.toString());
        return bookingRepository.findTopByClientIdAndStatusNotInOrderByCreatedAtDesc(
                client.getId(), finishedStatuses);
    }

    @Override
    public Bookings getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking with ID " + bookingId + " not found"));
    }

    @Override
    public List<BookingDTO> getBookingsByClientEmail(String email) {
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client account not found with email: " + email));
        List<Bookings> bookings = bookingRepository.findByClientId(client.getId());
        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BookingResponseDTO> getAllBookings(Pageable pageable) {
        Page<Bookings> bookingPage = bookingRepository.findAll(pageable);

        return bookingPage.map(booking -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setId(booking.getId());
            dto.setType(booking.getType());
            dto.setPickupTime(booking.getPickupTime());
            dto.setAddressName(booking.getAddressName());
            dto.setLat(booking.getLat());
            dto.setLng(booking.getLng());
            dto.setAmountPaid(BigDecimal.valueOf(booking.getPkg().getPrice()));
            dto.setStatus(booking.getStatus());
            dto.setPaymentStatus(booking.getPaymentStatus());
            dto.setCreatedAt(booking.getCreatedAt());
            dto.setUpdatedAt(booking.getUpdatedAt());
            if (booking.getPhotographer() != null) {
                UserSummaryDTO photographerDto = new UserSummaryDTO();
                photographerDto.setId(booking.getPhotographer().getId());
                photographerDto.setName(booking.getPhotographer().getName());
                dto.setPhotographer(photographerDto);
            }
            if (booking.getClient() != null) {
                UserSummaryDTO clientDto = new UserSummaryDTO();
                clientDto.setId(booking.getClient().getId());
                clientDto.setName(booking.getClient().getName());
                dto.setClient(clientDto);
            }
            if (booking.getPkg() != null) {
                PackageSummaryDTO pkgDto = new PackageSummaryDTO();
                pkgDto.setId(booking.getPkg().getId());
                pkgDto.setName(booking.getPkg().getName());
                dto.setPkg(pkgDto);
            }
            if (booking.getReview() != null) {
                ReviewSummaryDTO reviewDto = new ReviewSummaryDTO();
                reviewDto.setId(booking.getReview().getId());
                dto.setReview(reviewDto);
            }

            return dto;
        });
    }
}