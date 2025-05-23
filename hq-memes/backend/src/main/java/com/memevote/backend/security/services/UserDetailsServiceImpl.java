package com.memevote.backend.security.services;

import com.memevote.backend.model.User;
import com.memevote.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by username/email: {}", username);

        // First try to find by username
        logger.info("Trying to find user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    // If not found by username, try by email
                    logger.info("User not found by username, trying email: {}", username);
                    return userRepository.findByEmail(username)
                            .orElseThrow(() -> {
                                logger.error("User not found with username/email: {}", username);
                                return new UsernameNotFoundException("User Not Found with username/email: " + username);
                            });
                });

        logger.info("User found: {}", user.getUsername());
        return UserDetailsImpl.build(user);
    }
}
