package cz.bedla.h2.repository;

import cz.bedla.h2.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
}
