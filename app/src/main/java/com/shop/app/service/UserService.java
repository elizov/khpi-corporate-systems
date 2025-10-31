package com.shop.app.service;

import com.shop.app.model.User;
import com.shop.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("User with this username already exists");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        user.setRole(user.getRole().toUpperCase(java.util.Locale.ROOT));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }
}
