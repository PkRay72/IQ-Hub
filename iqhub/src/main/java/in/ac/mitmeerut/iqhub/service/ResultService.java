package in.ac.mitmeerut.iqhub.service;

import in.ac.mitmeerut.iqhub.entity.Question;
import in.ac.mitmeerut.iqhub.entity.Result;
import in.ac.mitmeerut.iqhub.entity.ResultAnswer;
import in.ac.mitmeerut.iqhub.entity.Test;
import in.ac.mitmeerut.iqhub.entity.User;
import in.ac.mitmeerut.iqhub.repository.ResultAnswerRepository;
import in.ac.mitmeerut.iqhub.repository.ResultRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final ResultAnswerRepository resultAnswerRepository;

    public ResultService(ResultRepository resultRepository, ResultAnswerRepository resultAnswerRepository) {
        this.resultRepository = resultRepository;
        this.resultAnswerRepository = resultAnswerRepository;
    }

    public boolean hasAlreadyAttempted(User student, Test test) {
        return resultRepository.existsByStudentAndTest(student, test);
    }

    public Optional<Result> getExistingResult(User student, Test test) {
        List<Result> results = resultRepository.findByStudentAndTest(student, test);
        return results.stream().min(Comparator.comparing(Result::getSubmittedAt));
    }

    public Result submitTest(User student, Test test, Map<Long, String> answers,
                              int tabSwitchCount, boolean autoSubmitted) {

        if (hasAlreadyAttempted(student, test)) {
            throw new IllegalStateException("Aap ye test pehle hi attempt kar chuke hain. Dobara attempt allowed nahi hai.");
        }

        List<Question> questions = test.getQuestions();
        int total = questions.size();
        int correct = 0;

        List<ResultAnswer> answerRecords = new ArrayList<>();
        List<String> weakTopics = new ArrayList<>();

        for (Question q : questions) {
            String selected = answers.get(q.getId());
            boolean isCorrect = selected != null && selected.equalsIgnoreCase(q.getCorrectOption());
            if (isCorrect) {
                correct++;
            } else {
                weakTopics.add(q.getSubject() + " (" + q.getDifficulty() + ")");
            }

            ResultAnswer ra = new ResultAnswer();
            ra.setQuestion(q);
            ra.setSelectedOption(selected);
            ra.setCorrect(isCorrect);
            answerRecords.add(ra);
        }

        int wrong = total - correct;
        double percentage = total == 0 ? 0 : (correct * 100.0) / total;
        int rating = calculateRating(percentage);

        Result result = new Result();
        result.setStudent(student);
        result.setTest(test);
        result.setTotalQuestions(total);
        result.setCorrectCount(correct);
        result.setWrongCount(wrong);
        result.setScore(percentage);
        result.setRating(rating);
        result.setTabSwitchCount(tabSwitchCount);
        result.setAutoSubmitted(autoSubmitted);
        result.setReviewFeedback(generateFeedback(percentage, weakTopics, autoSubmitted));

        Result saved = resultRepository.save(result);

        for (ResultAnswer ra : answerRecords) {
            ra.setResult(saved);
        }
        resultAnswerRepository.saveAll(answerRecords);

        return saved;
    }

    private int calculateRating(double percentage) {
        if (percentage >= 90) return 5;
        if (percentage >= 75) return 4;
        if (percentage >= 60) return 3;
        if (percentage >= 40) return 2;
        return 1;
    }

    private String generateFeedback(double percentage, List<String> weakTopics, boolean autoSubmitted) {
        StringBuilder sb = new StringBuilder();

        if (percentage >= 90) {
            sb.append("Excellent performance! Keep it up.");
        } else if (percentage >= 75) {
            sb.append("Good job. A little more practice will make you perfect.");
        } else if (percentage >= 60) {
            sb.append("Decent attempt, but there is clear room for improvement.");
        } else if (percentage >= 40) {
            sb.append("You need more practice on the fundamentals of this subject.");
        } else {
            sb.append("Performance was weak. Please revise the topic thoroughly and reattempt.");
        }

        if (!weakTopics.isEmpty()) {
            sb.append(" Focus on: ").append(String.join(", ", distinctLimited(weakTopics, 5))).append(".");
        }

        if (autoSubmitted) {
            sb.append(" Note: This test was auto-submitted because you switched tabs/left the exam window "
                    + "multiple times. Please avoid switching tabs during an exam.");
        }

        return sb.toString();
    }

    private List<String> distinctLimited(List<String> list, int limit) {
        List<String> distinct = new ArrayList<>();
        for (String s : list) {
            if (!distinct.contains(s)) distinct.add(s);
            if (distinct.size() >= limit) break;
        }
        return distinct;
    }

    public List<Result> getResultsForStudent(User student) {
        return resultRepository.findByStudent(student);
    }

    public List<Result> getResultsForTest(Test test) {
        return resultRepository.findByTest(test);
    }

    public List<Result> getAllResults() {
        return resultRepository.findAll();
    }

    public Result getById(Long id) {
        return resultRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Result not found"));
    }

    public List<ResultAnswer> getAnswers(Result result) {
        return resultAnswerRepository.findByResult(result);
    }
}