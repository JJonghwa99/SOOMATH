package com.SOOBIN.SOOMATH.Chart;

import com.SOOBIN.SOOMATH.Member.Member;
import com.SOOBIN.SOOMATH.Member.MemberRepository;
import com.SOOBIN.SOOMATH.Member.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/score")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminScoreController {

    private final MemberRepository memberRepository;
    private final ScoreService scoreService;
    private final MyUserDetailsService myuserDetailsService;

    @Autowired
    public AdminScoreController(MemberRepository memberRepository, ScoreService scoreService, MyUserDetailsService myUserDetailsService) {
        this.memberRepository = memberRepository;
        this.scoreService = scoreService;
        this.myuserDetailsService = myUserDetailsService;
    }

    // 관리자 화면: 회원 목록 및 성적 파일 업로드
    @GetMapping
    public String adminScore(Model model) {
        List<MyUserDetailsService.MemberDTO> members = myuserDetailsService.getAllMembers();
        model.addAttribute("members", members);
        return "score";
    }

    // 성적 파일 업로드
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


}
