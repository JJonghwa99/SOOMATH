package com.SOOBIN.SOOMATH.Member;

import com.SOOBIN.SOOMATH.Member.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final TempMemberRepository tempMemberRepository;
    private final MemberRepository memberRepository;
    @Autowired
    private MyUserDetailsService myuserDetailsService;

    @GetMapping("/register")
    public String register() {
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
    public String adminMember(Model model){
        List<MyUserDetailsService.TempMemberDTO> tempMembers = myuserDetailsService.getAllTempMembers();
        List<MyUserDetailsService.MemberDTO> members = myuserDetailsService.getAllMembers();

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
}
