package com.avoworld.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class MainController {

    @GetMapping("/")
    public String main() {
        return "실뱌의 avoworld를 위한 서버 엔드포인트입니다.";
    }
}
