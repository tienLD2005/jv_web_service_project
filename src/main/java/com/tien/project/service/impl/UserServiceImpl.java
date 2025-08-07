package com.tien.project.service.impl;

import com.tien.project.config.jwt.JWTProvider;
import com.tien.project.config.principal.CustomUserDetails;
import com.tien.project.dto.request.UserLogin;
import com.tien.project.dto.request.UserRegister;
import com.tien.project.dto.request.UserUpdate;
import com.tien.project.dto.request.ChangePasswordRequest;
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
        // Kiểm tra trùng email
        if (userRepository.findByEmail(userRegister.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy vai trò CUSTOMER"));

        User user = User.builder()
                .username(userRegister.getUsername())
                .passwordHash(passwordEncoder.encode(userRegister.getPassword()))
                .email(userRegister.getEmail())
                .fullName(userRegister.getFullName())
                .phoneNumber(userRegister.getPhoneNumber())
                .isActive(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Tạo bản ghi UserRole
        UserRole userRole = new UserRole();
        userRole.setUserId(savedUser.getUserId());
        userRole.setRoleId(customerRole.getRoleId());
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        sendVerificationEmail(savedUser);

        return savedUser;
    }

    @Override
    public JWTResponse login(UserLogin userLogin) {
        User user = userRepository.findByUsername(userLogin.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!user.getEmailVerified()) {
            throw new IllegalArgumentException("Email chưa xác thực");
        }
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Tài khoản đã bị khóa");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtProvider.generateToken(userDetails.getUsername());

        Session session = new Session();
        session.setSessionId(token);
        session.setUserId(user.getUserId());
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
                .orElseThrow(() -> new EntityNotFoundException("Token không hợp lệ"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));
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
            helper.setSubject("Xác thực địa chỉ email của bạn");
            helper.setText(
                    "<h1>Xác thực Email</h1>" +
                            "<p>Vui lòng nhận token xác minh của bạn:</p>" +
                             token  +
                            "<p>Mã xác minh này sẽ hết hạn sau 24 giờ.</p>",
                    true
            );
            mailSender.send(message);
            log.info("Đã gửi email xác thực tới {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Không thể gửi email xác thực tới {}", user.getEmail(), e);
            throw new RuntimeException("Không thể gửi email xác thực");
        }
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));
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
    public void changePassword(String username, ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));
    }

    @Override
    public User updateUser(Integer id, UserUpdate userUpdate) {
        User user = getUserById(id);
        user.setFullName(userUpdate.getFullName());
        user.setPhoneNumber(userUpdate.getPhoneNumber());
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