package fr.spoutnik87.musicbot_rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public String handleError(HttpServletRequest request) {
        return String.format("<html><body><h2>Something went wrong!</h2><body></html>");
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
