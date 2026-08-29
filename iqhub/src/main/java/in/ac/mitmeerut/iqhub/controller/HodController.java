package in.ac.mitmeerut.iqhub.controller;

import in.ac.mitmeerut.iqhub.entity.QuestionStatus;
import in.ac.mitmeerut.iqhub.entity.User;
import in.ac.mitmeerut.iqhub.repository.UserRepository;
import in.ac.mitmeerut.iqhub.service.QuestionService;
import in.ac.mitmeerut.iqhub.service.ResultService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hod")
public class HodController {

    private final QuestionService questionService;
    private final ResultService resultService;
    private final UserRepository userRepository;

    public HodController(QuestionService questionService, ResultService resultService, UserRepository userRepository) {
        this.questionService = questionService;
        this.resultService = resultService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendingQuestions", questionService.getPendingQuestions());
        return "hod/dashboard";
    }

    @PostMapping("/questions/review/{id}")
    public String review(@PathVariable Long id,
                          @RequestParam QuestionStatus decision,
                          @RequestParam(required = false) String comment,
                          Authentication auth) {
        User hod = currentUser(auth);
        questionService.reviewQuestion(id, hod, decision, comment);
        return "redirect:/hod/dashboard";
    }

    @GetMapping("/results")
    public String allResults(Model model) {
        model.addAttribute("results", resultService.getAllResults());
        return "hod/results";
    }
}