package com.example.smatt_study_load.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return new UserDetailsImpl(user);
    }
}