package com.example.cvgenerator.controller;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CVService cvService;

    @Autowired
    public AdminController(UserService userService, CVService cvService) {
        this.userService = userService;
        this.cvService = cvService;
    }

    // Відображення списку всіх користувачів
    @GetMapping
    public String showAdminPanel(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/admin-panel";
    }

    // Відображення деталей користувача
    @GetMapping("/users/{id}")
    public String showUserDetails(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
        List<CV> userCVs = cvService.getAllCVsByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("cvList", userCVs);
        return "admin/user-details";
    }

    // Відображення форми редагування користувача
    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        // Створюємо копію користувача без пароля для форми
        User formUser = new User();
        formUser.setId(user.getId());
        formUser.setFirstName(user.getFirstName());
        formUser.setLastName(user.getLastName());
        formUser.setEmail(user.getEmail());
        formUser.setPhoneNumber(user.getPhoneNumber());
        formUser.setBirthDate(user.getBirthDate());
        formUser.setRole(user.getRole());
        formUser.setPassword(""); // Порожній пароль для форми

        model.addAttribute("user", formUser);
        return "admin/edit-user";
    }

    // Обробка форми редагування користувача
    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") User formUser,
                             @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm,
                             RedirectAttributes redirectAttrs,
                             Model model) {
        try {
            User existingUser = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

            // Оновлюємо основні дані користувача
            updateUserData(existingUser, formUser);

            // Перевіряємо паролі, якщо поле пароля не порожнє
            if (formUser.getPassword() != null && !formUser.getPassword().isEmpty()) {
                // Перевіряємо, чи співпадають паролі
                if (passwordConfirm == null || !formUser.getPassword().equals(passwordConfirm)) {
                    // Паролі не співпадають, повертаємося на форму з повідомленням
                    model.addAttribute("user", formUser);
                    model.addAttribute("passwordError", "Паролі не співпадають");
                    return "admin/edit-user";
                }

                // Паролі співпадають, оновлюємо пароль
                userService.updatePassword(existingUser, formUser.getPassword());
            }

            userService.saveUser(existingUser);
            redirectAttrs.addFlashAttribute("success", "Користувача успішно оновлено");

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin";
    }

    // Допоміжний метод для оновлення даних користувача
    private void updateUserData(User existingUser, User formUser) {
        // Оновлюємо поля лише якщо вони не пусті
        if (formUser.getFirstName() != null && !formUser.getFirstName().isEmpty()) {
            existingUser.setFirstName(formUser.getFirstName());
        }

        if (formUser.getLastName() != null && !formUser.getLastName().isEmpty()) {
            existingUser.setLastName(formUser.getLastName());
        }

        if (formUser.getPhoneNumber() != null && !formUser.getPhoneNumber().isEmpty()) {
            existingUser.setPhoneNumber(formUser.getPhoneNumber());
        }

        if (formUser.getBirthDate() != null) {
            existingUser.setBirthDate(formUser.getBirthDate());
        }

        if (formUser.getRole() != null && !formUser.getRole().isEmpty()) {
            existingUser.setRole(formUser.getRole());
        }
    }

    // Видалення користувача
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            userService.deleteUser(id);
            redirectAttrs.addFlashAttribute("success", "Користувача успішно видалено");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin";
    }

    // Відображення CV користувача
    @GetMapping("/cv/{id}")
    public String viewCV(@PathVariable Long id, Model model) {
        CV cv = cvService.getCVById(id)
                .orElseThrow(() -> new RuntimeException("CV не знайдено"));

        model.addAttribute("cv", cv);
        model.addAttribute("adminView", true);

        // Визначення шаблону для відображення CV
        String templateName = cv.getTemplate() != null ? cv.getTemplate().getName().toLowerCase() : "";

        if (templateName.contains("academic")) {
            return "cv-3";
        } else if (templateName.contains("creative")) {
            return "cv-2";
        } else {
            return "cv-1";
        }
    }

    // Видалення CV
    @PostMapping("/cv/{id}/delete")
    public String deleteCV(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            CV cv = cvService.getCVById(id)
                    .orElseThrow(() -> new RuntimeException("CV не знайдено"));

            Long userId = cv.getUser().getId();
            cvService.deleteCV(id);

            redirectAttrs.addFlashAttribute("success", "CV успішно видалено");
            return "redirect:/admin/users/" + userId;
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin";
        }
    }
}