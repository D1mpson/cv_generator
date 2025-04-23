package com.example.cvgenerator.controller;

import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;
    private final CVService cvService;

    @Autowired
    public UserController(UserService userService, CVService cvService) {
        this.userService = userService;
        this.cvService = cvService;
    }

    // Відображення форми реєстрації
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Обробка форми реєстрації
    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") User user,
                                      BindingResult result,
                                      RedirectAttributes redirectAttrs) {
        // Перевіряємо чи є помилки валідації
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.saveUser(user);
            redirectAttrs.addFlashAttribute("success", "Реєстрація пройшла успішно");
        } catch (RuntimeException e) {
            result.rejectValue("email", "error.user", e.getMessage());
            return "register";
        }

        return "redirect:/login?success";
    }

    // Відображення профілю користувача
    @GetMapping("/profile")
    public String showUserProfile(HttpServletRequest request, Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("httpServletRequest", request); // Додати цей рядок
        model.addAttribute("cvList", cvService.getAllCVsByUser(currentUser));
        return "profile";
    }

    // Відображення форми редагування профілю
    @GetMapping("/edit-profile")
    public String showEditProfileForm(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Створюємо копію користувача без пароля для форми
        User formUser = new User();
        formUser.setId(currentUser.getId());
        formUser.setFirstName(currentUser.getFirstName());
        formUser.setLastName(currentUser.getLastName());
        formUser.setEmail(currentUser.getEmail());
        formUser.setPhoneNumber(currentUser.getPhoneNumber());
        formUser.setBirthDate(currentUser.getBirthDate());
        formUser.setPassword(""); // Порожній пароль для форми

        model.addAttribute("user", formUser);
        return "edit-profile";
    }

    // Обробка форми редагування профілю
    @PostMapping("/edit-profile")
    public String updateProfile(@ModelAttribute("user") User formUser,
                                @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm,
                                RedirectAttributes redirectAttrs,
                                Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }

            // Оновлюємо дані користувача
            updateUserData(currentUser, formUser);

            // Перевіряємо паролі, якщо поле пароля не порожнє
            if (formUser.getPassword() != null && !formUser.getPassword().isEmpty()) {
                // Перевіряємо, чи співпадають паролі
                if (passwordConfirm == null || !formUser.getPassword().equals(passwordConfirm)) {
                    // Паролі не співпадають, повертаємося на форму з повідомленням
                    model.addAttribute("user", formUser);
                    model.addAttribute("passwordError", "Паролі не співпадають");
                    return "edit-profile";
                }

                // Паролі співпадають, оновлюємо пароль
                userService.updatePassword(currentUser, formUser.getPassword());
            }

            userService.saveUser(currentUser);
            redirectAttrs.addFlashAttribute("success", "Профіль успішно оновлено");

            // Логуємо дію для перевірки
            System.out.println("Профіль користувача '" + currentUser.getEmail() + "' успішно оновлено");

        } catch (Exception e) {
            // Логування помилки
            System.err.println("Помилка при оновленні профілю: " + e.getMessage());
            e.printStackTrace();
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/edit-profile";
        }

        return "redirect:/profile?success";
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
    }
}