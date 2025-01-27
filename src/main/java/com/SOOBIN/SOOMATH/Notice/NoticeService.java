package com.SOOBIN.SOOMATH.Notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NoticeService {
    @Autowired
    private NoticeRepository noticeRepository;

    public Optional<Notice> getLatestNotice() {
        return noticeRepository.findTopByOrderByCreatedAtDesc(); // 최신 공지사항 조회
    }
}
