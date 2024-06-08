package com.triptune.domain.home.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "메인화면 관련 API")
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<?> home(){
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
