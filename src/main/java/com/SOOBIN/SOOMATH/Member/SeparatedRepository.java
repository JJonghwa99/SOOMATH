package com.SOOBIN.SOOMATH.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeparatedRepository extends JpaRepository<Separated,String> {
    Optional<Separated> findBysCode(String sCode);
}
