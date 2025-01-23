package com.SOOBIN.SOOMATH;

import com.SOOBIN.SOOMATH.QnaBoard.QnaPostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class NavController {
    private final QnaPostService qnaPostService;

    public NavController(QnaPostService qnaPostService) {
        this.qnaPostService = qnaPostService;
    }

    @GetMapping("")
    String home(Model model) {
        List<Map<String, Object>> rankings = qnaPostService.getTopNicknames();
        model.addAttribute("rankings", rankings);
        return "main.html";
    }

    @GetMapping("/login")
    String login(){
        return "login.html";
    }



}
