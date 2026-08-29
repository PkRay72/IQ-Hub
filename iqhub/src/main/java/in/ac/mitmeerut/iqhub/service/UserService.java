package in.ac.mitmeerut.iqhub.service;

import in.ac.mitmeerut.iqhub.entity.Role;
import in.ac.mitmeerut.iqhub.entity.User;
import in.ac.mitmeerut.iqhub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Kam se kam 8 characters, ek uppercase, ek lowercase, ek number, ek special character
    private static final Pattern STRONG_PASSWORD =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!_\\-.]).{8,}$");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void validatePasswordStrength(String password) {
        if (password == null || !STRONG_PASSWORD.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "Password kamzor hai. Kam se kam 8 characters, ek CAPITAL letter, ek small letter, " +
                "ek number aur ek special character (@#$%^&+=!) hona chahiye.");
        }
    }

    public User register(String username, String rawPassword, String fullName, String email, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        validatePasswordStrength(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        return userRepository.save(user);
    }

    public User updateProfile(Long userId, String fullName, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use by another account");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        validatePasswordStrength(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}