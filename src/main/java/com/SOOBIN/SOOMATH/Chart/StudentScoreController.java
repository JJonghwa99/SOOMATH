package com.SOOBIN.SOOMATH.Chart;

import com.SOOBIN.SOOMATH.Member.Member;
import com.SOOBIN.SOOMATH.Member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class StudentScoreController {

    private final ScoreService scoreService;
    private final MemberRepository memberRepository;

    @Autowired
    public StudentScoreController(ScoreService scoreService, MemberRepository memberRepository) {
        this.scoreService = scoreService;
        this.memberRepository = memberRepository;
    }

    // 학생 개인 성적 차트
    @GetMapping("/score")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public String Score(Model model) {
        // 현재 인증된 사용자의 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // username으로 Member 찾기
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // displayName을 모델에 추가
        model.addAttribute("displayName", member.getDisplayName());

        // username에 해당하는 성적 통계 데이터를 가져와 모델에 추가
        Map<String, Object> scoreData = scoreService.getStudentScores(username);
        model.addAllAttributes(scoreData);

        return "sview";
    }
}


