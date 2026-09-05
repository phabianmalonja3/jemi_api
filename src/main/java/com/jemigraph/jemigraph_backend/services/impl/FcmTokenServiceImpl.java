package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.FcmTokenDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmTokenServiceImpl implements FcmTokenService {
        private final UserRepository userRepository;
    @Override
    public void updateFcmToken(String email, FcmTokenDTO fcmTokenDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));
        user.setFcmToken(fcmTokenDTO.getToken());
        user.setDeviceType(fcmTokenDTO.getDeviceType());
        userRepository.save(user);
    }
}
