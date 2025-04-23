package com.example.cvgenerator.controller;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.TemplateService;
import com.example.cvgenerator.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CVController {

    private final CVService cvService;
    private final TemplateService templateService;
    private final UserService userService;

    @Autowired
    public CVController(CVService cvService, TemplateService templateService, UserService userService) {
        this.cvService = cvService;
        this.templateService = templateService;
        this.userService = userService;
    }

    @GetMapping("/all-cv")
    public String showAllCV(Model model) {
        model.addAttribute("cvList", cvService.getAllCVsForCurrentUser());
        return "all-cv";
    }

    @GetMapping("/generator")
    public String showGeneratorForm(Model model) {
        model.addAttribute("cv", new CV());
        model.addAttribute("templates", templateService.getAllTemplates());
        model.addAttribute("fonts", new String[]{"Arial", "Times New Roman", "Calibri", "Verdana", "Roboto"});
        return "generator";
    }

    @PostMapping("/generator")
    public String createCV(@Valid @ModelAttribute("cv") CV cv,
                           BindingResult result,
                           @RequestParam("templateId") Long templateId,
                           @RequestParam("photo") MultipartFile photo,
                           @RequestParam(value = "portfolioLinks", required = false) List<String> portfolioLinks,
                           @RequestParam(value = "knownLanguages", required = false) List<String> knownLanguages,
                           @RequestParam(value = "educationItems", required = false) List<String> educationItems,
                           @RequestParam(value = "coursesItems", required = false) List<String> coursesItems,
                           @RequestParam(value = "workExperienceItems", required = false) List<String> workExperienceItems,
                           @RequestParam(value = "softSkillsItems", required = false) List<String> softSkillsItems,
                           @RequestParam(value = "hardSkillsItems", required = false) List<String> hardSkillsItems,
                           RedirectAttributes redirectAttrs,
                           Model model) {

        if (result.hasErrors()) {
            model.addAttribute("templates", templateService.getAllTemplates());
            model.addAttribute("fonts", new String[]{"Arial", "Times New Roman", "Calibri", "Verdana", "Roboto"});
            return "generator";
        }

        try {
            Template template = templateService.getTemplateById(templateId)
                    .orElseThrow(() -> new RuntimeException("Шаблон не знайдено"));

            // Перевірка, що htmlPath не null
            if (template.getHtmlPath() == null) {
                // Встановлюємо значення за замовчуванням, якщо htmlPath не встановлено
                template.setHtmlPath("cv-1");
                // Зберігаємо оновлений шаблон
                templateService.saveTemplate(template);
            }

            cv.setTemplate(template);

            // Обробка освіти
            if (educationItems != null) {
                StringBuilder educationBuilder = new StringBuilder();
                for (String item : educationItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!educationBuilder.isEmpty()) {
                            educationBuilder.append(", ");
                        }
                        educationBuilder.append(item.trim());
                    }
                }
                cv.setEducation(educationBuilder.toString());
            }

            // Обробка курсів
            if (coursesItems != null) {
                StringBuilder coursesBuilder = new StringBuilder();
                for (String item : coursesItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!coursesBuilder.isEmpty()) {
                            coursesBuilder.append(", ");
                        }
                        coursesBuilder.append(item.trim());
                    }
                }
                cv.setCourses(coursesBuilder.toString());
            }

            // Обробка досвіду роботи
            if (workExperienceItems != null) {
                StringBuilder workExperienceBuilder = new StringBuilder();
                for (String item : workExperienceItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!workExperienceBuilder.isEmpty()) {
                            workExperienceBuilder.append(", ");
                        }
                        workExperienceBuilder.append(item.trim());
                    }
                }
                cv.setWorkExperience(workExperienceBuilder.toString());
            }

            // Обробка софт скілів
            if (softSkillsItems != null) {
                StringBuilder softSkillsBuilder = new StringBuilder();
                for (String item : softSkillsItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!softSkillsBuilder.isEmpty()) {
                            softSkillsBuilder.append(", ");
                        }
                        softSkillsBuilder.append(item.trim());
                    }
                }
                cv.setSoftSkills(softSkillsBuilder.toString());
            }

            // Обробка хард скілів
            if (hardSkillsItems != null) {
                StringBuilder hardSkillsBuilder = new StringBuilder();
                for (String item : hardSkillsItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!hardSkillsBuilder.isEmpty()) {
                            hardSkillsBuilder.append(", ");
                        }
                        hardSkillsBuilder.append(item.trim());
                    }
                }
                cv.setHardSkills(hardSkillsBuilder.toString());
            }

            // Встановлюємо нові поля, якщо вони надійшли з форми
            if (portfolioLinks != null) {
                // Перетворюємо масив у список і видаляємо порожні значення
                List<String> linksList = new ArrayList<>();
                for (String link : portfolioLinks) {
                    if (link != null && !link.trim().isEmpty()) {
                        linksList.add(link);
                    }
                }
                cv.setPortfolioLinks(linksList);
            }

            if (knownLanguages != null) {
                cv.setKnownLanguages(knownLanguages);
            }

            CV savedCV = cvService.createCV(cv, photo);
            redirectAttrs.addFlashAttribute("success", "CV успішно створено!");
            return "redirect:/cv/" + savedCV.getId();
        } catch (IOException e) {
            result.rejectValue("photoPath", "error.cv", "Помилка при завантаженні фото");
            model.addAttribute("templates", templateService.getAllTemplates());
            model.addAttribute("fonts", new String[]{"Arial", "Times New Roman", "Calibri", "Verdana", "Roboto"});
            return "generator";
        } catch (Exception e) {
            result.rejectValue("name", "error.cv", e.getMessage());
            model.addAttribute("templates", templateService.getAllTemplates());
            model.addAttribute("fonts", new String[]{"Arial", "Times New Roman", "Calibri", "Verdana", "Roboto"});
            return "generator";
        }
    }

    @GetMapping("/cv/{id}")
    public String viewCV(@PathVariable Long id, Model model) {
        CV cv = cvService.getCVById(id)
                .orElseThrow(() -> new RuntimeException("CV не знайдено"));

        // Перевірка прав доступу
        if (!cv.getUser().getId().equals(userService.getCurrentUser().getId())) {
            return "redirect:/profile?error=unauthorized";
        }

        // Логування для діагностики
        Template template = cv.getTemplate();
        System.out.println("Перегляд CV #" + id);
        System.out.println("Шаблон: " + (template != null ? template.getName() + " (ID: " + template.getId() + ")" : "null"));
        System.out.println("HTML шлях: " + (template != null ? template.getHtmlPath() : "null"));

        model.addAttribute("cv", cv);

        // Безпечна перевірка шляху HTML файлу
        String htmlPath = template != null && template.getHtmlPath() != null
                ? template.getHtmlPath()
                : "cv-1"; // Якщо шлях null, використовуємо cv-1 як значення за замовчуванням

        // Виправлення - використання шаблону відповідно до назви/ID шаблону
        if (template != null) {
            String templateName = template.getName().toLowerCase();

            if (templateName.contains("academic")) {
                return "cv-3";
            } else if (templateName.contains("creative")) {
                return "cv-2";
            } else if (templateName.contains("professional")) {
                return "cv-1";
            }
        }

        // Якщо не змогли визначити за назвою, спробуємо за htmlPath
        if (htmlPath.contains("cv-3")) {
            return "cv-3";
        } else if (htmlPath.contains("cv-2")) {
            return "cv-2";
        } else if (htmlPath.contains("cv-1")) {
            return "cv-1";
        } else {
            // Якщо шаблон не розпізнано, використовуємо перший шаблон
            return "cv-1";
        }
    }

    @GetMapping("/cv/{id}/edit")
    public String showEditCVForm(@PathVariable Long id, Model model) {
        CV cv = cvService.getCVById(id)
                .orElseThrow(() -> new RuntimeException("CV не знайдено"));

        // Перевірка прав доступу
        if (!cv.getUser().getId().equals(userService.getCurrentUser().getId())) {
            return "redirect:/profile?error=unauthorized";
        }

        model.addAttribute("cv", cv);
        model.addAttribute("templates", templateService.getAllTemplates());
        model.addAttribute("fonts", new String[]{"Arial", "Times New Roman", "Calibri", "Verdana", "Roboto"});

        // Додаємо інформацію про вибраний шаблон
        if (cv.getTemplate() != null) {
            model.addAttribute("selectedTemplateId", cv.getTemplate().getId());
        }

        return "generator"; // Використовуємо той самий шаблон для редагування
    }

    @PostMapping("/cv/{id}/edit")
    public String updateCV(@PathVariable Long id,
                           @Valid @ModelAttribute("cv") CV cv,
                           BindingResult result,
                           @RequestParam("templateId") Long templateId,
                           @RequestParam("photo") MultipartFile photo,
                           @RequestParam(value = "portfolioLinks[]", required = false) String[] portfolioLinks,
                           @RequestParam(value = "knownLanguages", required = false) List<String> knownLanguages,
                           @RequestParam(value = "educationItems[]", required = false) String[] educationItems,
                           @RequestParam(value = "coursesItems[]", required = false) String[] coursesItems,
                           @RequestParam(value = "workExperienceItems[]", required = false) String[] workExperienceItems,
                           @RequestParam(value = "softSkillsItems[]", required = false) String[] softSkillsItems,
                           @RequestParam(value = "hardSkillsItems[]", required = false) String[] hardSkillsItems,
                           RedirectAttributes redirectAttrs,
                           Model model) {

        if (result.hasErrors()) {
            model.addAttribute("templates", templateService.getAllTemplates());
            model.addAttribute("fonts", new String[]{"Arial", "Times New Roman", "Calibri", "Verdana", "Roboto"});
            return "generator";
        }

        try {
            // Отримуємо існуючий CV з бази даних
            CV existingCV = cvService.getCVById(id)
                    .orElseThrow(() -> new RuntimeException("CV не знайдено"));

            // Встановлюємо ID для оновлення існуючого запису
            cv.setId(id);
            cv.setUser(existingCV.getUser());

            // Отримуємо вибраний шаблон
            Template template = templateService.getTemplateById(templateId)
                    .orElseThrow(() -> new RuntimeException("Шаблон не знайдено"));

            // Виводимо інформацію для перевірки
            System.out.println("Оновлення CV #" + id);
            System.out.println("Вибраний шаблон: " + template.getName() + " (ID: " + template.getId() + ")");

            // Перевірка, що htmlPath не null
            if (template.getHtmlPath() == null) {
                // Встановлюємо значення за замовчуванням, якщо htmlPath не встановлено
                template.setHtmlPath("cv-1");
                // Зберігаємо оновлений шаблон
                templateService.saveTemplate(template);
            }

            // Присвоюємо шаблон до CV
            cv.setTemplate(template);

            // Обробка освіти
            if (educationItems != null) {
                StringBuilder educationBuilder = new StringBuilder();
                for (String item : educationItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!educationBuilder.isEmpty()) {
                            educationBuilder.append(", ");
                        }
                        educationBuilder.append(item.trim());
                    }
                }
                cv.setEducation(educationBuilder.toString());
            } else {
                cv.setEducation(existingCV.getEducation());
            }

            // Обробка курсів
            if (coursesItems != null) {
                StringBuilder coursesBuilder = new StringBuilder();
                for (String item : coursesItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!coursesBuilder.isEmpty()) {
                            coursesBuilder.append(", ");
                        }
                        coursesBuilder.append(item.trim());
                    }
                }
                cv.setCourses(coursesBuilder.toString());
            } else {
                cv.setCourses(existingCV.getCourses());
            }

            // Обробка досвіду роботи
            if (workExperienceItems != null) {
                StringBuilder workExperienceBuilder = new StringBuilder();
                for (String item : workExperienceItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!workExperienceBuilder.isEmpty()) {
                            workExperienceBuilder.append(", ");
                        }
                        workExperienceBuilder.append(item.trim());
                    }
                }
                cv.setWorkExperience(workExperienceBuilder.toString());
            } else {
                cv.setWorkExperience(existingCV.getWorkExperience());
            }

            // Обробка софт скілів
            if (softSkillsItems != null) {
                StringBuilder softSkillsBuilder = new StringBuilder();
                for (String item : softSkillsItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!softSkillsBuilder.isEmpty()) {
                            softSkillsBuilder.append(", ");
                        }
                        softSkillsBuilder.append(item.trim());
                    }
                }
                cv.setSoftSkills(softSkillsBuilder.toString());
            } else {
                cv.setSoftSkills(existingCV.getSoftSkills());
            }

            // Обробка хард скілів
            if (hardSkillsItems != null) {
                StringBuilder hardSkillsBuilder = new StringBuilder();
                for (String item : hardSkillsItems) {
                    if (item != null && !item.trim().isEmpty()) {
                        if (!hardSkillsBuilder.isEmpty()) {
                            hardSkillsBuilder.append(", ");
                        }
                        hardSkillsBuilder.append(item.trim());
                    }
                }
                cv.setHardSkills(hardSkillsBuilder.toString());
            } else {
                cv.setHardSkills(existingCV.getHardSkills());
            }

            // Встановлюємо нові поля, якщо вони надійшли з форми
            if (portfolioLinks != null) {
                // Перетворюємо масив у список і видаляємо порожні значення
                List<String> linksList = new ArrayList<>();
                for (String link : portfolioLinks) {
                    if (link != null && !link.trim().isEmpty()) {
                        linksList.add(link);
                    }
                }
                cv.setPortfolioLinks(linksList);
            } else {
                cv.setPortfolioLinks(existingCV.getPortfolioLinks());
            }

            if (knownLanguages != null) {
                cv.setKnownLanguages(knownLanguages);
            } else {
                cv.setKnownLanguages(existingCV.getKnownLanguages());
            }

            // Оновлюємо CV у базі даних
            cvService.updateCV(cv, photo);

            redirectAttrs.addFlashAttribute("success", "CV успішно оновлено!");
            return "redirect:/cv/" + id;
        } catch (Exception e) {
            // Логуємо помилку для відстеження
            System.err.println("Помилка при оновленні CV: " + e.getMessage());
            e.printStackTrace();

            result.rejectValue("name", "error.cv", e.getMessage());
            model.addAttribute("templates", templateService.getAllTemplates());
            model.addAttribute("fonts", new String[]{"Arial", "Times New Roman", "Calibri", "Verdana", "Roboto"});
            return "generator";
        }
    }

    @PostMapping("/cv/{id}/delete")
    public String deleteCV(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            cvService.deleteCV(id);
            redirectAttrs.addFlashAttribute("success", "CV успішно видалено!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/all-cv";
    }
}