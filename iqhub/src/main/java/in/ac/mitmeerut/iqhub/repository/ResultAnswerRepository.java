package in.ac.mitmeerut.iqhub.repository;

import in.ac.mitmeerut.iqhub.entity.Result;
import in.ac.mitmeerut.iqhub.entity.ResultAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultAnswerRepository extends JpaRepository<ResultAnswer, Long> {
    List<ResultAnswer> findByResult(Result result);
}
