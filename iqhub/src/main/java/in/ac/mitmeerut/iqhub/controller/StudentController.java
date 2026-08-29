package in.ac.mitmeerut.iqhub.controller;

import in.ac.mitmeerut.iqhub.dto.SubmitTestRequest;
import in.ac.mitmeerut.iqhub.entity.Result;
import in.ac.mitmeerut.iqhub.entity.Test;
import in.ac.mitmeerut.iqhub.entity.User;
import in.ac.mitmeerut.iqhub.repository.UserRepository;
import in.ac.mitmeerut.iqhub.service.ResultService;
import in.ac.mitmeerut.iqhub.service.TestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final TestService testService;
    private final ResultService resultService;
    private final UserRepository userRepository;

    public StudentController(TestService testService, ResultService resultService, UserRepository userRepository) {
        this.testService = testService;
        this.resultService = resultService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User student = currentUser(auth);
        model.addAttribute("tests", testService.getPublishedTests());
        model.addAttribute("student", student);
        model.addAttribute("resultService", resultService);
        return "student/dashboard";
    }

    @GetMapping("/test/{id}/start")
    public String startTest(@PathVariable Long id, Model model, Authentication auth) {
        User student = currentUser(auth);
        Test test = testService.getById(id);

        // Agar pehle se attempt kiya hua hai to seedha result dikhao, dobara attempt na hone do
        Optional<Result> existing = resultService.getExistingResult(student, test);
        if (existing.isPresent()) {
            return "redirect:/student/result/" + existing.get().getId();
        }

        model.addAttribute("test", test);
        return "student/attempt-test";
    }

    @PostMapping("/test/{id}/submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitTest(@PathVariable Long id,
                                           @RequestBody SubmitTestRequest request,
                                           Authentication auth) {
        User student = currentUser(auth);
        Test test = testService.getById(id);

        Map<String, Object> response = new HashMap<>();

        // Double-check: submit ke waqt bhi verify karo koi already-attempted result to nahi ban gaya
        if (resultService.hasAlreadyAttempted(student, test)) {
            Optional<Result> existing = resultService.getExistingResult(student, test);
            response.put("error", "Aap ye test pehle hi attempt kar chuke hain.");
            response.put("redirect", "/student/result/" + existing.get().getId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        Map<Long, String> answers = new HashMap<>();
        if (request.getAnswers() != null) {
            request.getAnswers().forEach(a -> answers.put(a.getQuestionId(), a.getSelectedOption()));
        }

        Result result = resultService.submitTest(student, test, answers,
                request.getTabSwitchCount(), request.isAutoSubmitted());

        response.put("resultId", result.getId());
        response.put("redirect", "/student/result/" + result.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results")
    public String myResults(Model model, Authentication auth) {
        User student = currentUser(auth);
        model.addAttribute("results", resultService.getResultsForStudent(student));
        return "student/results";
    }

    @GetMapping("/result/{id}")
    public String resultDetail(@PathVariable Long id, Model model) {
        Result result = resultService.getById(id);
        model.addAttribute("result", result);
        model.addAttribute("answers", resultService.getAnswers(result));
        return "student/result-detail";
    }
}