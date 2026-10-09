package com.titan.controller;

import com.titan.entity.Announcement;
import com.titan.repository.AnnouncementRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementController(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    // ADMIN - announcements page
    @GetMapping("/announcements")
    public String announcements(Model model) {
        model.addAttribute("announcements", announcementRepository.findAll());
        return "announcements";
    }

    // ADMIN - create announcement
    @PostMapping("/announcements/add")
    public String addAnnouncement(@ModelAttribute Announcement announcement) {

        announcement.setCreatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);

        return "redirect:/announcements";
    }

    // ADMIN - delete announcement
    @PostMapping("/announcements/delete/{id}")
    public String deleteAnnouncement(@PathVariable Long id) {

        announcementRepository.deleteById(id);

        return "redirect:/announcements";
    }

    // MEMBER - view announcements
    @GetMapping("/member/announcements")
    public String memberAnnouncements(Model model) {

        model.addAttribute(
                "announcements",
                announcementRepository.findAll()
        );

        return "member/announcements";
    }
}
