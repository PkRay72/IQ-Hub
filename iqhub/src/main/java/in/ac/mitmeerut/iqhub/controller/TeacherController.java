package in.ac.mitmeerut.iqhub.controller;

import in.ac.mitmeerut.iqhub.entity.*;
import in.ac.mitmeerut.iqhub.repository.UserRepository;
import in.ac.mitmeerut.iqhub.service.AiQuestionService;
import in.ac.mitmeerut.iqhub.service.NotificationService;
import in.ac.mitmeerut.iqhub.service.QuestionService;
import in.ac.mitmeerut.iqhub.service.ResultService;
import in.ac.mitmeerut.iqhub.service.TestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final QuestionService questionService;
    private final NotificationService notificationService;
    private final TestService testService;
    private final ResultService resultService;
    private final UserRepository userRepository;
    private final AiQuestionService aiQuestionService;

    public TeacherController(QuestionService questionService, NotificationService notificationService,
                              TestService testService, ResultService resultService, UserRepository userRepository,
                              AiQuestionService aiQuestionService) {
        this.questionService = questionService;
        this.notificationService = notificationService;
        this.testService = testService;
        this.resultService = resultService;
        this.userRepository = userRepository;
        this.aiQuestionService = aiQuestionService;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) ExamCategory category,
                             Model model, Authentication auth) {
        User teacher = currentUser(auth);

        List<Question> questions = (category != null)
                ? questionService.getQuestionsByTeacherAndCategory(teacher, category)
                : questionService.getQuestionsByTeacher(teacher);

        model.addAttribute("questions", questions);
        model.addAttribute("categories", ExamCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("unreadCount", notificationService.getForUser(teacher).stream()
                .filter(n -> !n.isRead()).count());
        return "teacher/dashboard";
    }

    @GetMapping("/questions/add")
    public String addQuestionForm(Model model) {
        model.addAttribute("question", new Question());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("categories", ExamCategory.values());
        return "teacher/add-question";
    }

    @PostMapping("/questions/add")
    public String addQuestion(@ModelAttribute Question question, Authentication auth) {
        User teacher = currentUser(auth);
        questionService.addQuestion(question, teacher);
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/questions/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        User teacher = currentUser(auth);
        Question q = questionService.getQuestionsByTeacher(teacher).stream()
                .filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        model.addAttribute("question", q);
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("categories", ExamCategory.values());
        return "teacher/add-question";
    }

    @PostMapping("/questions/edit/{id}")
    public String resubmit(@PathVariable Long id, @ModelAttribute Question question, Authentication auth) {
        User teacher = currentUser(auth);
        questionService.resubmitQuestion(id, question, teacher);
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/notifications")
    public String notifications(Model model, Authentication auth) {
        User teacher = currentUser(auth);
        model.addAttribute("notifications", notificationService.getForUser(teacher));
        notificationService.markAllRead(teacher);
        return "teacher/notifications";
    }

    @GetMapping("/tests/create")
    public String createTestForm(Model model, Authentication auth) {
        User teacher = currentUser(auth);
        model.addAttribute("approvedQuestions",
                questionService.getQuestionsByTeacher(teacher).stream()
                        .filter(q -> q.getStatus() == QuestionStatus.APPROVED)
                        .toList());
        return "teacher/create-test";
    }

    @PostMapping("/tests/create")
    public String createTest(@RequestParam String title,
                              @RequestParam String subject,
                              @RequestParam int durationMinutes,
                              @RequestParam List<Long> questionIds,
                              Authentication auth) {
        User teacher = currentUser(auth);
        List<Question> questions = questionIds.stream()
                .map(qid -> questionService.getQuestionsByTeacher(teacher).stream()
                        .filter(q -> q.getId().equals(qid) && q.getStatus() == QuestionStatus.APPROVED)
                        .findFirst().orElseThrow())
                .toList();
        testService.createTest(title, subject, durationMinutes, questions, teacher, true);
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/my-tests")
    public String myTests(Model model, Authentication auth) {
        User teacher = currentUser(auth);
        model.addAttribute("tests", testService.getTestsByTeacher(teacher));
        return "teacher/my-tests";
    }

    @GetMapping("/tests/{id}/results")
    public String testResults(@PathVariable Long id, Model model) {
        Test test = testService.getById(id);
        model.addAttribute("test", test);
        model.addAttribute("results", resultService.getResultsForTest(test));
        return "teacher/test-results";
    }

    // ===== AI QUESTION GENERATION (ab preview + confirm ke saath) =====

    @GetMapping("/questions/ai-generate")
    public String aiGenerateForm(Model model) {
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("categories", ExamCategory.values());
        return "teacher/ai-generate";
    }

    // Step 1: Generate karo, HOD ko mat bhejo abhi - session me store karo, teacher ko dikhao
    @PostMapping("/questions/ai-generate")
    public String aiGenerate(@RequestParam String topic,
                              @RequestParam String subject,
                              @RequestParam ExamCategory category,
                              @RequestParam Difficulty difficulty,
                              @RequestParam int count,
                              Model model,
                              HttpSession session,
                              Authentication auth) {

        try {
            List<Question> generated = aiQuestionService.generateQuestions(topic, subject, category, difficulty, count);
            session.setAttribute("aiPreviewQuestions", generated);
            model.addAttribute("previewQuestions", generated);
        } catch (Exception e) {
            model.addAttribute("error", "AI generation fail ho gaya: " + e.getMessage());
        }

        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("categories", ExamCategory.values());
        return "teacher/ai-generate";
    }

    // Step 2: Teacher confirm dabaye tabhi HOD ke pending queue me jaayega
    @SuppressWarnings("unchecked")
    @PostMapping("/questions/ai-confirm")
    public String aiConfirm(HttpSession session, Model model, Authentication auth) {
        User teacher = currentUser(auth);

        List<Question> pending = (List<Question>) session.getAttribute("aiPreviewQuestions");
        if (pending != null && !pending.isEmpty()) {
            for (Question q : pending) {
                questionService.addQuestion(q, teacher);
            }
            session.removeAttribute("aiPreviewQuestions");
            model.addAttribute("success", pending.size() + " questions HOD review ke liye bhej diye gaye.");
        } else {
            model.addAttribute("error", "Koi preview question nahi mila. Pehle generate karo.");
        }

        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("categories", ExamCategory.values());
        return "teacher/ai-generate";
    }

    // Teacher discard bhi kar sakta hai agar questions pasand na aayein
    @PostMapping("/questions/ai-discard")
    public String aiDiscard(HttpSession session, Model model) {
        session.removeAttribute("aiPreviewQuestions");
        model.addAttribute("success", "Discarded. Aap dobara generate kar sakte ho.");
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("categories", ExamCategory.values());
        return "teacher/ai-generate";
    }
}