package com.SOOBIN.SOOMATH.Member;

import com.SOOBIN.SOOMATH.Member.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MemberController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    private final TempMemberRepository tempMemberRepository;
    private final MemberRepository memberRepository;
    private final SeparatedRepository SeparatedRepository;
    @Autowired
    private MyUserDetailsService myuserDetailsService;

    @GetMapping("/register")
    public String register(Model model) {
        List<Separated> courses = SeparatedRepository.findAll();

        model.addAttribute("courses", courses);

        return "register.html";
    }

    @GetMapping("/success")
    public String successPage() {
        return "success";
    }

    @PostMapping("/tempMember")
    @ResponseBody
    public String addMember(@RequestParam String username,
                            @RequestParam String displayName,
                            @RequestParam String nickName,
                            @RequestParam List<String> separated,
                            @RequestParam String schoolName,
                            @RequestParam int grade,
                            @RequestParam String password,
                            @RequestParam(defaultValue = "false") boolean auth) {
        try {
            if (password.length() < 8) {
                return "error2";
            }

            if (tempMemberRepository.findByUsername(username).isPresent() ||
                    memberRepository.findByUsername(username).isPresent()) {
                return "error1";
            }

            TempMember tempMember = new TempMember();
            tempMember.setUsername(username);
            tempMember.setDisplayName(displayName);
            tempMember.setNickName(nickName);
            tempMember.setSeparated(separated);
            tempMember.setSchoolName(schoolName);
            tempMember.setGrade(grade);
            tempMember.setAuth(auth);
            String encodedPassword = passwordEncoder.encode(password);
            tempMember.setPassword(encodedPassword);
            tempMemberRepository.save(tempMember);

            System.out.println(tempMember);

            return "success";

        } catch (Exception e) {
            return "error";
        }
    }
    @GetMapping("/admin/members")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminMember(Model model) {
        List<MyUserDetailsService.TempMemberDTO> tempMembers = myuserDetailsService.getAllTempMembers();
        List<MyUserDetailsService.MemberDTO> members = myuserDetailsService.getAllMembers();

        // 모든 tempMembers의 separated 필드를 하나의 리스트로 병합
        List<String> allSCodes2 = tempMembers.stream()
                .flatMap(member -> member.getSeparated().stream())
                .distinct() // 중복 제거
                .collect(Collectors.toList());

        // 병합된 sCode 리스트를 기준으로 sName 조회
        Map<String, String> sCodeToSNameMap2 = SeparatedRepository.findBySCodeIn(allSCodes2).stream()
                .collect(Collectors.toMap(Separated::getSCode, Separated::getSName));

        // 각 멤버의 분반 이름 매핑 추가
        tempMembers.forEach(member -> {
            List<String> tempMemberSeparatedNames = member.getSeparated().stream()
                    .map(sCodeToSNameMap2::get)
                    .collect(Collectors.toList());
            member.setSeparatedNames(tempMemberSeparatedNames);
        });

        // 모든 members의 separated 필드를 하나의 리스트로 병합
        List<String> allSCodes1 = members.stream()
                .flatMap(member -> member.getSeparated().stream())
                .distinct() // 중복 제거
                .collect(Collectors.toList());

        // 병합된 sCode 리스트를 기준으로 sName 조회
        Map<String, String> sCodeToSNameMap = SeparatedRepository.findBySCodeIn(allSCodes1).stream()
                .collect(Collectors.toMap(Separated::getSCode, Separated::getSName));

        // 각 멤버의 분반 이름 매핑 추가
        members.forEach(member -> {
            List<String> memberSeparatedNames = member.getSeparated().stream()
                    .map(sCodeToSNameMap::get)
                    .collect(Collectors.toList());
            member.setSeparatedNames(memberSeparatedNames);
        });

        List<Separated> courses = SeparatedRepository.findAll();

        model.addAttribute("courses", courses);
        model.addAttribute("tempMembers", tempMembers);
        model.addAttribute("members", members);

        return "members";
    }


    // 승인 메서드
    @Transactional
    @PostMapping("/admin/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseBody
    public String approveMember(@RequestParam Long id) {
        try {
            tempMemberRepository.findById(id).ifPresent(tempMember -> {
                tempMember.setAuth(true);
                tempMemberRepository.save(tempMember);
            });
            return "approved"; // 단순 문자열 반환
        } catch (Exception e) {
            return "error";
        }
    }

    // 거절 메서드
    @Transactional
    @DeleteMapping("/admin/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseBody
    public String rejectMember(@RequestParam Long id) {
        try {
            tempMemberRepository.deleteById(id);
            return "rejected"; // 단순 문자열 반환
        } catch (Exception e) {
            return "error";
        }
    }

    // 회원 정보 수정 메서드 (학교, 학년)
    @Transactional
    @PutMapping("/admin/updateMember")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseBody
    public String updateMember(@RequestBody Member member) {
        try {
            memberRepository.findById(member.getId()).ifPresent(existingMember -> {
                existingMember.setSchoolName(member.getSchoolName());
                existingMember.setGrade(member.getGrade());
                existingMember.setSeparated(member.getSeparated());
                memberRepository.save(existingMember);
            });
            return "updated";
        } catch (Exception e) {
            return "error";
        }
    }

    // 회원 삭제 메서드
    @Transactional
    @DeleteMapping("/admin/deleteMember")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseBody
    public String deleteMember(@RequestParam Long id) {
        try {
            memberRepository.deleteById(id);
            return "deleted";
        } catch (Exception e) {
            return "error";
        }
    }
    @GetMapping("/admin/separated")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String separated(Model model) {
        // 분반 데이터를 데이터베이스에서 가져오기
        List<Separated> separatedDetails = SeparatedRepository.findAll(); // Repository를 통해 모든 분반 정보 조회
        model.addAttribute("separatedDetails", separatedDetails); // 모델에 데이터 추가
        return "editSeparated"; // Thymeleaf 템플릿 이름
    }
    /*// 분반 저장 (수정)
    @PutMapping("/admin/separated/save")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> saveSeparated(@RequestBody Separated separated) {
        Optional<Separated> existingSeparated = SeparatedRepository.findBysCode(separated.getSCode());
        if (existingSeparated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 분반 코드입니다.");
        }
        existingSeparated.get().setSName(separated.getSName());
        SeparatedRepository.save(existingSeparated.get());
        return ResponseEntity.ok("✅수정완료");
    }*/

    @DeleteMapping("/admin/separated/delete/{sCode}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteSeparated(@PathVariable String sCode) {
        try {
            SeparatedRepository.deleteById(sCode); // 데이터베이스에서 삭제
            return ResponseEntity.ok("✅삭제완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌삭제 실패!");
        }
    }

    /*// 분반 추가
    @PostMapping("/admin/separated/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addSeparated(@RequestBody Separated separated) {
        if (SeparatedRepository.findBysCode(separated.getSCode()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 분반 코드입니다.");
        }
        SeparatedRepository.save(separated);
        return ResponseEntity.ok("✅추가완료");
    }*/

}
