package com.tien.project.service.impl;

import com.tien.project.config.jwt.JWTProvider;
import com.tien.project.config.principal.CustomUserDetails;
import com.tien.project.dto.request.UserLogin;
import com.tien.project.dto.request.UserRegister;
import com.tien.project.dto.request.UserUpdate;
import com.tien.project.dto.response.JWTResponse;
import com.tien.project.entity.*;
import com.tien.project.repository.*;
import com.tien.project.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTProvider jwtProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public User registerUser(UserRegister userRegister) {
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new EntityNotFoundException("Customer role not found"));

        User user = User.builder()
                .username(userRegister.getUsername())
                .passwordHash(passwordEncoder.encode(userRegister.getPassword()))
                .email(userRegister.getEmail())
                .fullName(userRegister.getFullName())
                .phoneNumber(userRegister.getPhoneNumber())
                .isActive(true)
                .emailVerified(false)
                .build(); // Không gán roles trực tiếp ở đây

        User savedUser = userRepository.save(user);

        // Tạo bản ghi UserRole
        UserRole userRole = new UserRole();
        userRole.setUserId(savedUser.getUserId());
        userRole.setRoleId(customerRole.getRoleId());
        userRole.setAssignedAt(LocalDateTime.now()); // Gán giá trị cho assigned_at
        userRoleRepository.save(userRole);

//        Customer customer = new Customer();
//        customer.setUserId(savedUser.getUserId());
//        customer.setStatus(Customer.CustomerStatus.ACTIVE);
//        customerRepository.save(customer);

        sendVerificationEmail(savedUser);

        return savedUser;
    }

    @Override
    public JWTResponse login(UserLogin userLogin) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtProvider.generateToken(userDetails.getUsername());

        Session session = new Session();
        session.setSessionId(token);
        session.setUserId(userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found")).getUserId());
        session.setExpiryTime(LocalDateTime.now().plusSeconds(jwtProvider.getJwtExpire() / 1000));
        sessionRepository.save(session);

        return JWTResponse.builder()
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .phone(userDetails.getPhone())
                .enabled(userDetails.isEnabled())
                .authorities(userDetails.getAuthorities())
                .token(token)
                .build();
    }

    @Override
    public boolean verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        return true;
    }

    @Override
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserId(user.getUserId());
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your Email Address");
            helper.setText("Please click the following link to verify your email: " +
                    "http://localhost:8080/api/v1/auth/verify-email/" + token);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send verification email", e);
            throw new RuntimeException("Failed to send verification email");
        }
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public User updateProfile(String username, UserUpdate userUpdate) {
        User user = getUserByUsername(username);
        user.setFullName(userUpdate.getFullName());
        user.setPhoneNumber(userUpdate.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public void changePassword(String username, String newPassword) {
//        User user = getUserByUsername(username);
//        user.setPasswordHash(passwordEncoder.encode(newPassword));
//        user.setUpdatedAt(LocalDateTime.now());
//        userRepository.save(user);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public User updateUser(Integer id, UserUpdate userUpdate) {
        User user = getUserById(id);
        user.setFullName(userUpdate.getFullName());
        user.setPhoneNumber(userUpdate.getPhoneNumber());
//        user.setEmail(userUpdate.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public void updateUserStatus(Integer id, Boolean isActive) {
        User user = getUserById(id);
        user.setIsActive(isActive);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer id) {
        User user = getUserById(id);
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}