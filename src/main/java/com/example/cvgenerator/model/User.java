package com.example.cvgenerator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Імʼя є обовʼязковим")
    private String firstName;

    @NotBlank(message = "Прізвище є обовʼязковим")
    private String lastName;

    @NotBlank(message = "Email є обовʼязковим")
    @Email(message = "Некоректний формат email")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Пароль є обовʼязковим")
    @Size(min = 6, message = "Пароль повинен містити не менше 6 символів")
    private String password;

    @NotBlank(message = "Номер телефону є обовʼязковим")
    @Pattern(regexp = "(\\+380[0-9]{9}|0[0-9]{9})", message = "Некоректний формат номеру телефону")    private String phoneNumber;

    @NotNull(message = "Дата народження є обовʼязковою")
    @Past(message = "Дата народження повинна бути в минулому")
    private LocalDate birthDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CV> cvList = new ArrayList<>();

    private String role = "ROLE_USER";
}