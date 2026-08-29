package in.ac.mitmeerut.iqhub.service;

import in.ac.mitmeerut.iqhub.entity.Question;
import in.ac.mitmeerut.iqhub.entity.Test;
import in.ac.mitmeerut.iqhub.entity.User;
import in.ac.mitmeerut.iqhub.repository.TestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public Test createTest(String title, String subject, int durationMinutes,
                            List<Question> questions, User teacher, boolean publishNow) {
        Test test = new Test();
        test.setTitle(title);
        test.setSubject(subject);
        test.setDurationMinutes(durationMinutes);
        test.setQuestions(questions);
        test.setPublishedBy(teacher);
        test.setPublished(publishNow);
        return testRepository.save(test);
    }

    public List<Test> getPublishedTests() {
        return testRepository.findByPublishedTrue();
    }

    public List<Test> getTestsByTeacher(User teacher) {
        return testRepository.findByPublishedBy(teacher);
    }

    public Test getById(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));
    }
}