package com.SOOBIN.SOOMATH.QnaBoard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QnaPostRepository extends JpaRepository<QnaPost, Long> {
    Page<QnaPostDto> findAllProjectedBy(Pageable pageable);

    List<QnaPost> findByTitleContainsAndSecretFalse(String keyword);

}
