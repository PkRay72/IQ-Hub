package in.ac.mitmeerut.iqhub.service;

import in.ac.mitmeerut.iqhub.entity.*;
import in.ac.mitmeerut.iqhub.repository.NotificationRepository;
import in.ac.mitmeerut.iqhub.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final NotificationRepository notificationRepository;

    public QuestionService(QuestionRepository questionRepository, NotificationRepository notificationRepository) {
        this.questionRepository = questionRepository;
        this.notificationRepository = notificationRepository;
    }

    public Question addQuestion(Question question, User teacher) {
        question.setCreatedBy(teacher);
        question.setStatus(QuestionStatus.PENDING);
        question.setReviewComment(null);
        question.setCreatedAt(LocalDateTime.now());
        return questionRepository.save(question);
    }

    public Question resubmitQuestion(Long questionId, Question updated, User teacher) {
        Question existing = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (!existing.getCreatedBy().getId().equals(teacher.getId())) {
            throw new SecurityException("You can only edit your own questions");
        }

        existing.setQuestionText(updated.getQuestionText());
        existing.setOptionA(updated.getOptionA());
        existing.setOptionB(updated.getOptionB());
        existing.setOptionC(updated.getOptionC());
        existing.setOptionD(updated.getOptionD());
        existing.setCorrectOption(updated.getCorrectOption());
        existing.setSubject(updated.getSubject());
        existing.setDifficulty(updated.getDifficulty());
        existing.setExamCategory(updated.getExamCategory());
        existing.setStatus(QuestionStatus.PENDING);
        existing.setReviewComment(null);

        Question saved = questionRepository.save(existing);

        notify(existing.getCreatedBy(), "Your improved question #" + saved.getId()
                + " has been resubmitted for HOD review.");
        return saved;
    }

    public List<Question> getPendingQuestions() {
        return questionRepository.findByStatus(QuestionStatus.PENDING);
    }

    public List<Question> getQuestionsByTeacher(User teacher) {
        return questionRepository.findByCreatedBy(teacher);
    }

    public List<Question> getQuestionsByTeacherAndCategory(User teacher, ExamCategory category) {
        return questionRepository.findByCreatedByAndExamCategory(teacher, category);
    }

    public List<Question> getApprovedQuestionsBySubject(String subject) {
        return questionRepository.findBySubjectAndStatus(subject, QuestionStatus.APPROVED);
    }

    public List<Question> getApprovedQuestionsByCategory(ExamCategory category) {
        return questionRepository.findByExamCategoryAndStatus(category, QuestionStatus.APPROVED);
    }

    public Question reviewQuestion(Long questionId, User hod, QuestionStatus decision, String comment) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        question.setStatus(decision);
        question.setReviewComment(comment);
        question.setReviewedBy(hod);
        question.setReviewedAt(LocalDateTime.now());
        Question saved = questionRepository.save(question);

        String message;
        switch (decision) {
            case APPROVED -> message = "Your question #" + saved.getId() + " was approved by HOD.";
            case NEEDS_IMPROVEMENT -> message = "HOD requested improvement on question #" + saved.getId()
                    + (comment != null && !comment.isBlank() ? ": " + comment : "")
                    + ". Please edit and resubmit.";
            case REJECTED -> message = "Your question #" + saved.getId() + " was rejected by HOD."
                    + (comment != null && !comment.isBlank() ? " Reason: " + comment : "");
            default -> message = "Your question #" + saved.getId() + " status changed.";
        }
        notify(saved.getCreatedBy(), message);

        return saved;
    }

    private void notify(User user, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        notificationRepository.save(n);
    }
}