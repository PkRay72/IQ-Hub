package in.ac.mitmeerut.iqhub.repository;

import in.ac.mitmeerut.iqhub.entity.Notification;
import in.ac.mitmeerut.iqhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
}