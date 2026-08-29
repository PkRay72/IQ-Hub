package in.ac.mitmeerut.iqhub.repository;

import in.ac.mitmeerut.iqhub.entity.ExamCategory;
import in.ac.mitmeerut.iqhub.entity.Question;
import in.ac.mitmeerut.iqhub.entity.QuestionStatus;
import in.ac.mitmeerut.iqhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByStatus(QuestionStatus status);
    List<Question> findByCreatedBy(User createdBy);
    List<Question> findBySubjectAndStatus(String subject, QuestionStatus status);
    List<Question> findByExamCategoryAndStatus(ExamCategory examCategory, QuestionStatus status);
    List<Question> findByCreatedByAndExamCategory(User createdBy, ExamCategory examCategory);
}