package com.buissnes.earnkowlege.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("")
class DemoController {

    @GetMapping("/greeting")
    fun greeting() = "Hello User"

}
