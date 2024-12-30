package com.SOOBIN.SOOMATH.ExamBoard;

import com.SOOBIN.SOOMATH.Member.Member;
import com.SOOBIN.SOOMATH.Member.MemberRepository;
import com.SOOBIN.SOOMATH.Member.MyUserDetailsService;
import com.SOOBIN.SOOMATH.Member.TempMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exam")
public class ExamController {
    private final ExamPostRepository examPostRepository;
    private final MemberRepository memberRepository;
    @Getter
    @Setter
    public class ExamPostDTO {
        private Long id;
        private String title;
        private String writer;
        private LocalDateTime createdDate;
        public ExamPostDTO(Long id, String title, String writer, LocalDateTime createdDate) {
            this.id = id;
            this.title = title;
            this.writer = writer;
            this.createdDate = createdDate;
        }
    }
    public List<ExamPostDTO> getAllExamPostList() {
        List<ExamPost> examPosts = examPostRepository.findAll();
        return examPosts.stream()
                .map(examPost -> new ExamPostDTO(
                        examPost.getId(),
                        examPost.getTitle(),
                        examPost.getWriter(),
                        examPost.getCreatedDate()
                        ))
                .collect(Collectors.toList());
    }

    public ExamController(ExamPostRepository examPostRepository, MemberRepository memberRepository) {
        this.examPostRepository = examPostRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/page/{i}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String ExamPagination(Model model, @PathVariable Integer i) {
        Page<ExamPost> result = examPostRepository.findAll(PageRequest.of(i - 1, 9, Sort.by(Sort.Order.desc("id"))));

        List<ExamPostDTO> postDTOs = result.getContent().stream()
                .map(examPost -> new ExamPostDTO(
                        examPost.getId(),
                        examPost.getTitle(),
                        examPost.getWriter(),
                        examPost.getCreatedDate()
                ))
                .collect(Collectors.toList());

        int totalPages = result.getTotalPages();
        int startPage = ((i - 1) / 10) * 10 + 1;
        int endPage = Math.min(startPage + 9, totalPages);

        model.addAttribute("posts", postDTOs);
        model.addAttribute("currentPage", i);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "examList";
    }
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String ExamPostSearch(@RequestParam String keyword, Model model) {
        List<ExamPost> examPosts = examPostRepository.findByTitleContains(keyword);

        List<ExamPostDTO> postDTOs = examPosts.stream()
                .map(examPost -> new ExamPostDTO(
                        examPost.getId(),
                        examPost.getTitle(),
                        examPost.getWriter(),
                        examPost.getCreatedDate()
                ))
                .collect(Collectors.toList());

        model.addAttribute("posts", postDTOs);
        model.addAttribute("keyword",keyword);
        model.addAttribute("currentPage", 1); // 검색 시 현재 페이지를 1로 설정
        model.addAttribute("totalPages", 1); // 검색 결과 페이지 수를 1로 설정
        model.addAttribute("startPage", 1);
        model.addAttribute("endPage", 1);

        return "examList";
    }




    @GetMapping("/write")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String showWriteForm() {
        return "examWrite";
    }

    @PostMapping("/write")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String savePost(@RequestParam String title,
                           @RequestParam String content,
                           Authentication authentication) {
        String username = authentication.getName();
        Optional<Member> optionalMember = memberRepository.findByUsername(username);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            ExamPost examPost = new ExamPost();
            examPost.setTitle(title);
            content = content.replaceAll(",\\s*$", "");
            examPost.setContent(content);
            examPost.setWriter(member.getDisplayName());
            examPostRepository.save(examPost);
        } else {
            System.out.println(username+"를 찾을 수 없습니다.");
        }

        return "redirect:/exam/page/1";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String viewPost(@PathVariable Long id, Model model) {
        Optional<ExamPost> optionalExamPost = examPostRepository.findById(id);
        if (optionalExamPost.isPresent()) {
            ExamPost post = optionalExamPost.get();
            model.addAttribute("post", post);
            return "examDetail";  // 상세보기 화면
        } else {
            return "redirect:/exam/page/1";  // 글이 없으면 목록으로 이동
        }
    }

    @Transactional
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<ExamPost> optionalExamPost = examPostRepository.findById(id);
            model.addAttribute("post", optionalExamPost.get());
            return "examWriteEdit";  // 수정 화면
    }

    @Transactional
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String editPost(@PathVariable Long id, @ModelAttribute ExamPost updatedPost) {
        // 데이터베이스에서 게시글을 찾음
        Optional<ExamPost> optionalExamPost = examPostRepository.findById(id);

        // 게시글이 존재하면
        if (optionalExamPost.isPresent()) {
            ExamPost examPost = optionalExamPost.get();

            // 제목과 내용 업데이트
            examPost.setTitle(updatedPost.getTitle());
            String content = updatedPost.getContent();
            content = content.replaceAll(",\\s*$", "");
            examPost.setContent(content);

            // 업데이트된 게시글을 저장
            examPostRepository.save(examPost);  // 이 부분이 데이터를 저장
        }
        // 수정된 게시글로 리다이렉트
        return "redirect:/exam/" + id;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deletePost(@PathVariable Long id, Authentication authentication) {
        Optional<ExamPost> optionalExamPost = examPostRepository.findById(id);
            examPostRepository.delete(optionalExamPost.get());
        return "redirect:/exam/page/1";
    }

    @PostMapping("/uploadImage")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @ResponseBody
    public Map<String, Object> uploadImage(MultipartHttpServletRequest request, HttpServletRequest req) throws Exception {
        Map<String, Object> map = new HashMap<>();
        MultipartFile uploadFile = request.getFile("upload");

        if (uploadFile != null) {
            String originalFileName = uploadFile.getOriginalFilename();
            String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
            String folderPath = req.getSession().getServletContext().getRealPath("/resources/static/examImage/");
            String newFileName = UUID.randomUUID() + ext;
            File file = new File(folderPath + newFileName);
            file.getParentFile().mkdirs();
            uploadFile.transferTo(file);

            map.put("uploaded", true);
            map.put("url", "/resources/static/examImage/" + newFileName);
        } else {
            map.put("uploaded", false);
            map.put("error", Map.of("message", "File upload failed."));
        }

        return map;
    }
}
