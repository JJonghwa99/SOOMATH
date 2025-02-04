package com.SOOBIN.SOOMATH.Chart;

import com.SOOBIN.SOOMATH.Member.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/score")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminScoreController {

    private final MemberRepository memberRepository;
    private final ScoreService scoreService;
    private final MyUserDetailsService myuserDetailsService;
    private final SeparatedRepository separatedRepository;
    private final ScoreRepository scoreRepository;

    @Autowired
    public AdminScoreController(MemberRepository memberRepository, ScoreService scoreService, MyUserDetailsService myUserDetailsService, SeparatedRepository separatedRepository, ScoreRepository scoreRepository) {
        this.memberRepository = memberRepository;
        this.scoreService = scoreService;
        this.myuserDetailsService = myUserDetailsService;
        this.separatedRepository = separatedRepository;
        this.scoreRepository = scoreRepository;
    }

    @GetMapping
    public String adminScore(Model model) {
        List<MyUserDetailsService.MemberDTO> members = myuserDetailsService.getAllMembers();
        List<Separated> courses = separatedRepository.findAll();

        model.addAttribute("courses", courses);
        model.addAttribute("members", members);
        return "score";
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadScoreFile(@RequestParam("file") MultipartFile file) {
        try {
            scoreService.parseAndSaveScores(file);
            return ResponseEntity.ok("✅성적이 성공적으로 업로드되었습니다.😊");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌파일 업로드에 실패했습니다😰.");
        }
    }

    @GetMapping("/image")
    public String scoreImage(@RequestParam String username, Model model) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<String> allSCodes1 = member.getSeparated().stream()
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> sCodeToSNameMap = separatedRepository.findBySCodeIn(allSCodes1).stream()
                .collect(Collectors.toMap(Separated::getSCode, Separated::getSName));

        Map<String, List<String>> sCodeToSMonth = separatedRepository.findBySCodeIn(allSCodes1).stream()
                .collect(Collectors.toMap(
                        Separated::getSCode,
                        Separated::getSMonth,
                        (existing, replacement) -> existing // 중복 키 처리
                ));

        List<Map<String, String>> memberSeparatedDetails = member.getSeparated().stream()
                .map(code -> Map.of("code", code, "name", sCodeToSNameMap.getOrDefault(code, "Unknown")))
                .collect(Collectors.toList());

        model.addAttribute("separatedDetails", memberSeparatedDetails);
        model.addAttribute("sCodeToSMonth", sCodeToSMonth);
        model.addAttribute("username", username);
        model.addAttribute("displayName", member.getDisplayName());
        return "scoreImage";
    }

    @GetMapping("/getStudentScores")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStudentScores(@RequestParam String username) {
        Map<String, Object> data = scoreService.getStudentScores(username);
        if (data.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/chart")
    @ResponseBody
    public Map<String, Object> getChartData(@RequestParam String username, @RequestParam String code, @RequestParam String month) {
        Map<String, Object> chartData = scoreService.getScoresByCodeAndMonth(username, code, month);
        return chartData != null ? chartData : Map.of(); // 빈 맵 반환
    }
}
