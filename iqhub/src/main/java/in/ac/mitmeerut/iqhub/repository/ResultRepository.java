package in.ac.mitmeerut.iqhub.repository;

import in.ac.mitmeerut.iqhub.entity.Result;
import in.ac.mitmeerut.iqhub.entity.Test;
import in.ac.mitmeerut.iqhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByStudent(User student);
    List<Result> findByStudentAndTest(User student, Test test);
    boolean existsByStudentAndTest(User student, Test test);
    List<Result> findByTest(Test test);
}