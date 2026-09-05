package com.jemigraph.jemigraph_backend.services;


import com.jemigraph.jemigraph_backend.DTO.PhotographerProfileDTO;
import com.jemigraph.jemigraph_backend.DTO.UserDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.mappers.PhotographerMapper;
import com.jemigraph.jemigraph_backend.mappers.UserMapper;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PhotographerMapper photographerMappper;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));


        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );


    }
    public Page<PhotographerProfileDTO> getPhotographers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage = userRepository.findAllByRole(UserRole.PHOTOGRAPHER, pageable);

        return userPage.map(photographerMappper::toDto);
    }
    public Page<PhotographerProfileDTO> getPhotographersALll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage = userRepository.findAllByRole(UserRole.PHOTOGRAPHER, pageable);

        return userPage.map(photographerMappper::toDto);
    }




    public String saveProfileImage(MultipartFile file, String email) {
        try {

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));


            // Match your controller
            String uploadDir = "/opt/myapp/uploads/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            user.setProfileImageUrl(fileName);
            userRepository.save(user);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file. Error: " + e.getMessage());
        }
    }

    public String updateToken(String email,String token){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

   user.setFcmToken(token);


   userRepository.save(user);

   return  "Succesfull ";



    }

    @Transactional
    public void removeUser(UUID uuid){

        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + uuid));

        userRepository.delete(user);



    }

    @Transactional
    public void updateStatus(UUID uuid){

        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + uuid));
        user.setVerified(!user.isVerified());
        userRepository.save(user);


    }

    @Transactional
    public User updateUser(UUID id,UserDTO userDTO) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + id));
        user.setEmail(userDTO.getEmail());
        user.setRole(UserRole.valueOf(userDTO.getRole()));
        user.setName(userDTO.getName());
//        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
       return userRepository.save(user);




    }
}