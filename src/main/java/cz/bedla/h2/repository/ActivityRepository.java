package cz.bedla.h2.repository;

import cz.bedla.h2.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
