package com.SOOBIN.SOOMATH.ExamBoard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamPostRepository extends JpaRepository<ExamPost, Long> {
    Page<ExamPost> findPageBy(Pageable page);
    // 제목으로 검색
    List<ExamPost> findByTitleContains(String title);

}

