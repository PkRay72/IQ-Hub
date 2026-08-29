package in.ac.mitmeerut.iqhub.repository;

import in.ac.mitmeerut.iqhub.entity.Attendance;
import in.ac.mitmeerut.iqhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudent(User student);
}