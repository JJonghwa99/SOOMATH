package com.SOOBIN.SOOMATH.Chart;

import com.SOOBIN.SOOMATH.Member.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StudentScoreController {

    private final ScoreService scoreService;
    private final MemberRepository memberRepository;
    @Autowired
    private final SeparatedRepository SeparatedRepository;

    @Autowired
    public StudentScoreController(ScoreService scoreService, MemberRepository memberRepository, com.SOOBIN.SOOMATH.Member.SeparatedRepository separatedRepository) {
        this.scoreService = scoreService;
        this.memberRepository = memberRepository;
        SeparatedRepository = separatedRepository;
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

        // 분반 코드 리스트 가져오기
        List<String> allSCodes1 = member.getSeparated().stream()
                .distinct() // 중복 제거
                .collect(Collectors.toList());

        // sCode를 기준으로 sName 조회
        Map<String, String> sCodeToSNameMap = SeparatedRepository.findBySCodeIn(allSCodes1).stream()
                .collect(Collectors.toMap(Separated::getSCode, Separated::getSName));

        //sCode를 기준으로 sMonth 조회
        @NotNull Map<String, List<String>> sCodeToSMonth = SeparatedRepository.findBySCodeIn(allSCodes1).stream()
                .collect(Collectors.toMap(Separated::getSCode, Separated::getSMonth));

        // 분반 이름 매핑
        List<String> memberSeparatedNames = member.getSeparated().stream()
                .map(sCodeToSNameMap::get)
                .collect(Collectors.toList());
        // sCodeToSMonth 기본값 처리
        Map<String, List<String>> sCodeToSMonthWithDefaults = member.getSeparated().stream()
                .collect(Collectors.toMap(
                        code -> code,
                        code -> sCodeToSMonth.getOrDefault(code, List.of()) // 키가 없는 경우 빈 리스트 반환
                ));




        // 분반 이름과 코드를 함께 전달
        List<Map<String, String>> memberSeparatedDetails = member.getSeparated().stream()
                .map(code -> Map.of("code", code, "name", sCodeToSNameMap.get(code)))
                .collect(Collectors.toList());

        model.addAttribute("separatedDetails", memberSeparatedDetails);
        model.addAttribute("sCodeToSMonth", sCodeToSMonthWithDefaults);
        // 모델에 데이터 추가
        model.addAttribute("displayName", member.getDisplayName());


        return "sview";
    }

    @GetMapping("/score/chart")
    @ResponseBody
    public Map<String, Object> getChartData(@RequestParam String code, @RequestParam String month) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // `code`와 `month`에 맞는 데이터 가져오기
        Map<String, Object> chartData = scoreService.getScoresByCodeAndMonth(username, code, month);
        return chartData;
    }
}


