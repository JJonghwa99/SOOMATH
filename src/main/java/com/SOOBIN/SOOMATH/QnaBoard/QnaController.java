package com.SOOBIN.SOOMATH.QnaBoard;

import com.SOOBIN.SOOMATH.Member.Member;
import com.SOOBIN.SOOMATH.Member.MemberRepository;
import com.SOOBIN.SOOMATH.Member.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



import java.util.*;

@Controller
@RequestMapping("/qna")
@PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
public class QnaController {
    @Autowired
    private MyUserDetailsService myuserDetailsService;

    private final QnaPostRepository qnaPostRepository;
    private final MemberRepository memberRepository;

    public QnaController(QnaPostRepository qnaPostRepository, MemberRepository memberRepository) {
        this.qnaPostRepository = qnaPostRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/page/{i}")
    public String QnaPagination(Model model, @PathVariable Integer i) {
        Page<QnaPostDto> result = qnaPostRepository.findAllProjectedBy(
                PageRequest.of(i - 1, 9, Sort.by(Sort.Order.desc("createdDate"))));

        List<QnaPostDto> posts = result.getContent();
        List<QnaPostDto> organizedPosts = organizePosts(posts);

        int totalPages = result.getTotalPages();
        int startPage = Math.max(1, ((i - 1) / 10) * 10 + 1);
        int endPage = Math.min(startPage + 9, totalPages);

        // 현재 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("posts", organizedPosts);
        model.addAttribute("currentPage", i);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("isAdmin", isAdmin);

        return "qnaList";
    }

    private List<QnaPostDto> organizePosts(List<QnaPostDto> posts) {
        Map<Long, QnaPostDto> postMap = new HashMap<>();
        Map<Long, List<QnaPostDto>> childrenMap = new HashMap<>();

        for (QnaPostDto post : posts) {
            postMap.put(post.getId(), post);
            childrenMap.put(post.getId(), new ArrayList<>());
        }

        List<QnaPostDto> organizedPosts = new ArrayList<>();

        for (QnaPostDto post : posts) {
            if (post.getParentId() == null) {
                organizedPosts.add(post);
            } else {
                childrenMap.get(post.getParentId()).add(post);
            }
        }

        return organizeChildren(organizedPosts, childrenMap);
    }

    private List<QnaPostDto> organizeChildren(List<QnaPostDto> parents, Map<Long, List<QnaPostDto>> childrenMap) {
        List<QnaPostDto> result = new ArrayList<>();
        for (QnaPostDto parent : parents) {
            result.add(parent);
            List<QnaPostDto> children = childrenMap.get(parent.getId());
            if (children != null && !children.isEmpty()) {
                children.sort(Comparator.comparing(QnaPostDto::getCreatedDate));
                result.addAll(organizeChildren(children, childrenMap));
            }
        }
        return result;
    }


    @GetMapping("/search")
    public String QnaPostSearch(@RequestParam String keyword, Model model) {
        List<QnaPost> posts = qnaPostRepository.findByTitleContainsAndSecretFalse(keyword);

        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", 1); // 검색 시 현재 페이지를 1로 설정
        model.addAttribute("totalPages", 1); // 검색 결과 페이지 수를 1로 설정
        model.addAttribute("startPage", 1);
        model.addAttribute("endPage", 1);
        return "qnaList";
    }

    @GetMapping("/write")
    public String showWriteForm() {
        return "qnaWrite";
    }

    @GetMapping("/write/q")
    public String askExamForm(@RequestParam String title, Model model) {
        model.addAttribute("title", title + " 질문합니다.");
        return "qnaWrite2";
    }

    @PostMapping("/write")
    public String savePost(@RequestParam String title,
                           @RequestParam String content,
                           @RequestParam(required = false) Long parentId,
                           @RequestParam(required = false) boolean secret,
                           Authentication authentication) {
        String username = authentication.getName();
        Optional<Member> optionalMember = memberRepository.findByUsername(username);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            QnaPost qnaPost = new QnaPost();
            qnaPost.setTitle(title);
            content = content.replaceAll(",\\s*$", "");
            qnaPost.setContent(content);
            qnaPost.setWriter(member.getDisplayName());
            qnaPost.setUsername(username);
            qnaPost.setSecret(secret);

            if (parentId != null) {
                Optional<QnaPost> parentPost = qnaPostRepository.findById(parentId);
                parentPost.ifPresent(qnaPost::setParent);
            }

            qnaPostRepository.save(qnaPost);
        }

        return "redirect:/qna/page/1";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Authentication authentication, Model model) {
        Optional<QnaPost> optionalQnaPost = qnaPostRepository.findById(id);
        if (optionalQnaPost.isPresent()) {
            QnaPost post = optionalQnaPost.get();
            String currentUsername = authentication.getName();
            boolean isAdmin = myuserDetailsService.isAdmin(currentUsername);

            // 비밀글 처리: 작성자, 관리자, 부모글 작성자만 볼 수 있도록
            if (post.isSecret() &&
                    !post.getUsername().equals(currentUsername) &&
                    !isAdmin &&
                    !(post.getParent() != null && post.getParent().getUsername().equals(currentUsername))) {
                return "redirect:/qna/page/1";
            }

            model.addAttribute("post", post);
            return "qnaDetail";  // 상세보기 화면
        } else {
            return "redirect:/qna/page/1";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<QnaPost> optionalQnaPost = qnaPostRepository.findById(id);
        optionalQnaPost.ifPresent(post -> model.addAttribute("post", post));
        return "qnaWriteEdit";  // 수정 화면
    }

    @PostMapping("/edit/{id}")
    public String editPost(@PathVariable Long id, @ModelAttribute QnaPost updatedPost) {
        Optional<QnaPost> optionalQnaPost = qnaPostRepository.findById(id);
        if (optionalQnaPost.isPresent()) {
            QnaPost qnaPost = optionalQnaPost.get();
            qnaPost.setTitle(updatedPost.getTitle());
            String content = updatedPost.getContent();
            content = content.replaceAll(",\\s*$", "");
            qnaPost.setContent(content);
            qnaPost.setSecret(updatedPost.isSecret());
            qnaPostRepository.save(qnaPost);
        }
        return "redirect:/qna/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        qnaPostRepository.deleteById(id);
        return "redirect:/qna/page/1";
    }
    @GetMapping("/write/answer/{id}")
    public String showAnswerForm(@PathVariable Long id, Model model) {
        Optional<QnaPost> parentPost = qnaPostRepository.findById(id);
        if (parentPost.isPresent()) {
            model.addAttribute("parentPost", parentPost.get());
            return "qnaWriteAnswer";
        }
        return "redirect:/qna/page/1";
    }

    @PostMapping("/write/answer/{id}")
    public String saveAnswer(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(required = false) boolean secret,
                             Authentication authentication) {
        Optional<QnaPost> parentPost = qnaPostRepository.findById(id);
        if (parentPost.isPresent()) {
            QnaPost parent = parentPost.get();
            String username = authentication.getName();
            Optional<Member> optionalMember = memberRepository.findByUsername(username);

            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();
                QnaPost reply = new QnaPost();
                reply.setTitle(title);
                reply.setContent(content.replaceAll(",\\s*$", ""));
                reply.setWriter(member.getDisplayName());
                reply.setUsername(username);
                reply.setSecret(parent.isSecret() || secret);
                reply.setParent(parent);

                qnaPostRepository.save(reply);
            }
        }
        return "redirect:/qna/page/1";
    }
}
