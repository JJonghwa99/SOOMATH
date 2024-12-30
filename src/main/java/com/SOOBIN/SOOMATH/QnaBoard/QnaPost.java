package com.SOOBIN.SOOMATH.QnaBoard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class QnaPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String writer; // 리스트에서 표시할 거
    private String username; // 로그인한 아이디 (작성자 식별용)

    private LocalDateTime createdDate = LocalDateTime.now();

    private boolean secret; // 비밀글 여부

    // 부모글 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private QnaPost parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaPost> replies = new ArrayList<>();

    @Transient // 이 필드는 데이터베이스에 저장되지 않습니다.
    private int depth;
    public int getDepth() {
        if (this.parent == null) {
            return 0;
        }
        return 1 + this.parent.getDepth();
    }



}
