package com.SOOBIN.SOOMATH.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TempMemberRepository extends JpaRepository<TempMember, Long> {
    Optional<TempMember> findByUsername(String username);

}
