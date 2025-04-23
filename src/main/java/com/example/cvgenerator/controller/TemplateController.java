package com.example.cvgenerator.controller;

import com.example.cvgenerator.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TemplateController {

    private final TemplateService templateService;

    @Autowired
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }


    //Узнать зачем здесь этот метод, если он не задеян!!!
    @GetMapping("/templates")
    public String showTemplates(Model model) {
        model.addAttribute("templates", templateService.getAllTemplates());
        return "templates";
    }
}