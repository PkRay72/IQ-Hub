package in.ac.mitmeerut.iqhub.repository;

import in.ac.mitmeerut.iqhub.entity.Role;
import in.ac.mitmeerut.iqhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
}