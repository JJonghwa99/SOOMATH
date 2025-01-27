package com.SOOBIN.SOOMATH;

import com.SOOBIN.SOOMATH.Notice.Notice;
import com.SOOBIN.SOOMATH.Notice.NoticeService;
import com.SOOBIN.SOOMATH.QnaBoard.QnaPostService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class NavController {
    private final QnaPostService qnaPostService;
    private final NoticeService noticeService;

    public NavController(QnaPostService qnaPostService, NoticeService noticeService) {
        this.qnaPostService = qnaPostService;
        this.noticeService = noticeService;
    }

    @GetMapping("")
    String home(Model model, HttpServletRequest request) {
        List<Map<String, Object>> rankings = qnaPostService.getTopNicknames();
        Optional<Notice> notice = noticeService.getLatestNotice();
        if (notice.isPresent()) {
            model.addAttribute("notice", notice.get());
        }
        // 오늘 하루 보지 않기 기능 확인 (쿠키를 활용)
        Cookie[] cookies = request.getCookies();
        boolean skipPopup = false;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("skipNotice".equals(cookie.getName()) && "true".equals(cookie.getValue())) {
                    skipPopup = true;
                    break;
                }
            }
        }

        model.addAttribute("skipPopup", skipPopup);
        model.addAttribute("rankings", rankings);
        return "main.html";
    }

    @GetMapping("/login")
    String login(){
        return "login.html";
    }



}
