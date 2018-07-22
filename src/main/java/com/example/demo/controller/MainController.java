package com.example.demo.controller;

import com.example.demo.domain.Message;
import com.example.demo.domain.User;
import com.example.demo.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class MainController {
    @Autowired
    private MessageRepository repository;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/")
    public String greeting() {
        return "greeting";
    }

    @GetMapping("main")
    public String main(@RequestParam(required = false) String filter, Model model) {

        if (filter != null && !filter.isEmpty()) {
            model.addAttribute("messages", repository.findByTag(filter));
            model.addAttribute("filter", filter);
        } else {
            model.addAttribute("messages", repository.findAll());
        }

        return "main";
    }

    @PostMapping("main")
    public String add(@AuthenticationPrincipal User user,
                      @RequestParam("file") MultipartFile file,
                      @RequestParam String text, @RequestParam String tag, Model model) throws IOException {
        Message message = new Message(text, tag, user);
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + resultFilename));
            message.setFilename(resultFilename);
        }


        repository.save(message);
        model.addAttribute("messages", repository.findAll());

        return "main";
    }
}
