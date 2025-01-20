package com.SOOBIN.SOOMATH.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeparatedRepository extends JpaRepository<Separated,String> {
    Optional<Separated> findBysCode(String sCode);

    @Query("SELECT s FROM Separated s WHERE s.sCode IN :sCodes")
    List<Separated> findBySCodeIn(@Param("sCodes") List<String> sCodes);

}
