package com.example.demo.javaSrc.people;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeopleRepository extends JpaRepository<People, Long> {
    Optional<People> findByEmail(String email);
    List<People> findByRole(People.Role role);

    Optional<People> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<People.Role> findRoleByEmail(String email);

    Optional<Long> findIdByEmail(String email);
}
