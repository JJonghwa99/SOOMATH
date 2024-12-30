package com.SOOBIN.SOOMATH.QnaBoard;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class QnaPostDto {
    private Long id;
    private String title;
    private String writer;
    private LocalDateTime createdDate;
    private boolean secret;
    private Long parentId;
    private String username;
    private int depth;
    private String parentUsername;

    public QnaPostDto(Long id, String title, String writer, LocalDateTime createdDate, boolean secret, Long parentId, String username, String parentUsername) {
        this.id = id;
        this.title = title;
        this.writer = writer;
        this.createdDate = createdDate;
        this.secret = secret;
        this.parentId = parentId;
        this.username = username;
        this.parentUsername = parentUsername;
        this.depth = 0; // 초기 깊이는 0으로 설정
    }

    public static QnaPostDto fromEntity(QnaPost post) {
        QnaPostDto dto = new QnaPostDto(
                post.getId(),
                post.getTitle(),
                post.getWriter(),
                post.getCreatedDate(),
                post.isSecret(),
                post.getParent() != null ? post.getParent().getId() : null,
                post.getUsername(),
                post.getParent() != null ? post.getParent().getUsername() : null
        );
        dto.setDepth(calculateDepth(post));
        return dto;
    }

    private static int calculateDepth(QnaPost post) {
        int depth = 0;
        QnaPost parent = post.getParent();
        while (parent != null) {
            depth++;
            parent = parent.getParent();
        }
        return depth;
    }
}