package com.tien.project.security.principal;

import com.tien.project.entity.User;
import com.tien.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tồn tại username"));

        return CustomUserDetails.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .authorities(mapRolesToAuthorities(user))
                .build();
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(User user) {
        return user.getUserRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleName()))
                .toList();
    }

}