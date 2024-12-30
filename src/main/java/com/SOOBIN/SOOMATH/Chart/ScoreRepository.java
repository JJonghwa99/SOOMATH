package com.SOOBIN.SOOMATH.Chart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByMemberUsername(String username);
    List<Score> findByMonthAndSeparated(String month, String separated);



}

