package fr.spoutnik87.musicbot_rest.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class ErrorController : ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    fun handleError(request: HttpServletRequest): String {
        return String.format("<html><body><h2>Something went wrong!</h2><body></html>")
    }

    override fun getErrorPath() = "/error"
}