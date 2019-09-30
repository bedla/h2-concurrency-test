package cz.bedla.h2.repository;

import cz.bedla.h2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
