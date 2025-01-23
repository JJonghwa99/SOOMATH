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

        @Query("SELECT m.nickName, COUNT(qp) AS postCount " +
                "FROM QnaPost qp " +
                "JOIN Member m ON qp.username = m.username " +
                "WHERE qp.username NOT IN ('admin', 'aleph_sbt') " +
                "GROUP BY m.nickName " +
                "ORDER BY postCount DESC")
        List<Object[]> findTopNicknames(Pageable pageable);


    @Query(value = "SELECT m.nick_name AS nickName, COUNT(qp.id) AS postCount " +
            "FROM qna_post qp " +
            "JOIN member m ON qp.username = m.username " +
            "WHERE qp.username NOT IN ('admin', 'aleph_sbt') " +
            "GROUP BY m.nick_name " +
            "ORDER BY postCount DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Object[]> findTopNicknames();


}
