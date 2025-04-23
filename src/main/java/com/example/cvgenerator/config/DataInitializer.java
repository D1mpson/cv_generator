package com.example.cvgenerator.config;

import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.TemplateService;
import com.example.cvgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TemplateService templateService;
    private final UserService userService;

    @Autowired
    public DataInitializer(TemplateService templateService, UserService userService) {
        this.templateService = templateService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Ініціалізація шаблонів
        if (templateService.getAllTemplates().isEmpty()) {
            createDefaultTemplates();
        } else {
            verifyAndFixTemplates();
        }

        // Створення адміністратора
        createAdminIfNotExists();
    }

    private void createDefaultTemplates() {
        // Шаблон 1
        Template template1 = new Template();
        template1.setName("Professional");
        template1.setDescription("Класичний професійний шаблон, підходить для більшості галузей");
        template1.setHtmlPath("cv-1");
        template1.setPreviewImagePath("/images/generator/Professional.png");
        templateService.saveTemplate(template1);

        // Шаблон 2
        Template template2 = new Template();
        template2.setName("Creative");
        template2.setDescription("Сучасний креативний шаблон для дизайнерів та креативних професій");
        template2.setHtmlPath("cv-2");
        template2.setPreviewImagePath("/images/generator/Creative.png");
        templateService.saveTemplate(template2);

        // Шаблон 3
        Template template3 = new Template();
        template3.setName("Academic");
        template3.setDescription("Формальний шаблон для академічних та наукових позицій");
        template3.setHtmlPath("cv-3");
        template3.setPreviewImagePath("/images/generator/Academic.png");
        templateService.saveTemplate(template3);

        System.out.println("Створено нові шаблони");
    }

    private void verifyAndFixTemplates() {
        List<Template> templates = templateService.getAllTemplates();
        boolean updated = false;

        for (Template template : templates) {
            String newPreviewPath = null;

            if (template.getName().equalsIgnoreCase("Professional")) {
                newPreviewPath = "/images/generator/Professional.png";
            } else if (template.getName().equalsIgnoreCase("Creative")) {
                newPreviewPath = "/images/generator/Creative.png";
            } else if (template.getName().equalsIgnoreCase("Academic")) {
                newPreviewPath = "/images/generator/Academic.png";
            }

            if (newPreviewPath != null && !newPreviewPath.equals(template.getPreviewImagePath())) {
                template.setPreviewImagePath(newPreviewPath);
                templateService.saveTemplate(template);
                updated = true;
                System.out.println("  - Оновлено зображення для шаблону: " + template.getName());
            }
        }

        if (updated) {
            System.out.println("Оновлено шляхи до зображень шаблонів");
        }
    }

    private void createAdminIfNotExists() {
        if (userService.findByRole("ROLE_ADMIN").isEmpty()) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setEmail("admin@system.com");
            admin.setPassword("admin123");
            admin.setPhoneNumber("+380000000000");
            admin.setBirthDate(LocalDate.now().minusYears(20));
            admin.setRole("ROLE_ADMIN");
            userService.saveUser(admin);
            System.out.println("Створено адміністратора (email: admin@system.com, пароль: admin123)");
        }
    }
}