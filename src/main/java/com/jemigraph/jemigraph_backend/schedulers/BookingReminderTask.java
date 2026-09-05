package com.jemigraph.jemigraph_backend.schedulers;

import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingReminderTask {

    @Autowired
    private BookingRepository bookingRepository;

    // Inakimbia kila dakika 15 kuangalia bookings zinazokaribia
    @Scheduled(fixedRate = 900000)
    public void sendReminders() {
        LocalDateTime soon = LocalDateTime.now().plusHours(2); // Saa 2 kabla
//        List<Bookings> upcoming = bookingRepository.findUpcomingBookings(soon);

//        for(Bookings b : upcoming) {
//            // Hapa unatuma Push Notification au Email
//            // "Kumbuka kuwasha GPS yako, Mpiga picha wako anajiandaa!"
//        }
    }
}