package com.taskmanager.org.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeViewController {

    @GetMapping("/")
    public String home(Model model, Authentication authentication, Principal principal) {
        if(authentication != null){
            System.out.println("Principal: " + authentication.getPrincipal());
            System.out.println("Name: " + authentication.getName());
        } else {
            System.out.println("Brak zalogowanego użytkownika");
        }

        if (principal != null) {
            model.addAttribute("name", principal.getName());
        }

        return "index";
    }
}