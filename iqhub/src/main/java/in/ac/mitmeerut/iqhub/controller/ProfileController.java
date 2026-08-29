package in.ac.mitmeerut.iqhub.controller;

import in.ac.mitmeerut.iqhub.entity.User;
import in.ac.mitmeerut.iqhub.repository.UserRepository;
import in.ac.mitmeerut.iqhub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;

    public ProfileController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @GetMapping
    public String viewProfile(Model model, Authentication auth) {
        model.addAttribute("user", currentUser(auth));
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String fullName,
                                 @RequestParam String email,
                                 Model model, Authentication auth) {
        User user = currentUser(auth);
        try {
            userService.updateProfile(user.getId(), fullName, email);
            model.addAttribute("success", "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("user", currentUser(auth));
        return "profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  Model model, Authentication auth) {
        User user = currentUser(auth);
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("New password and confirm password do not match");
            }
            userService.changePassword(user.getId(), currentPassword, newPassword);
            model.addAttribute("success", "Password changed successfully.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("user", currentUser(auth));
        return "profile";
    }
}