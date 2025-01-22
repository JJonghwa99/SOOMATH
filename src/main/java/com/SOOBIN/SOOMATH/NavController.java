package com.SOOBIN.SOOMATH;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NavController {
    @GetMapping("")
    String home(){
        return "main.html";
    }

    @GetMapping("/login")
    String login(){
        return "login.html";
    }



}
