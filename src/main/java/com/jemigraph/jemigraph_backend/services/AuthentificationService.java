package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.*;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.requests.AuthenticationRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public interface AuthentificationService {


     AuthResponse authenticate(AuthenticationRequest request,String deviceName);
     User getMe(String email );
      boolean verifyOtp(String email, String userProvidedOtp);
     RegistrationResponseDTO createUser(RegisterRequestDTO userDto);
     boolean requestPasswordReset(String email);
     boolean completePasswordReset(String token, String newPassword);
     UserDTO changeUserPassword(String email, String currentPassword, String newPassword);

}
