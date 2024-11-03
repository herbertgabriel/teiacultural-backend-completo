package com.proa.teiacultural.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.proa.teiacultural.controller.dto.LoginRequest;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String telephone;

    @Column(unique = true)
    private String username;
    private String profilePicture;
    private String professionalName;
    private String category;
    private String aboutMe;
    private String socialMedia;
    private String localization;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    
    private Set<Role> roles;
    

    public boolean isLoginCorrect(LoginRequest loginRequest, PasswordEncoder passwordEncoder) {
        if (loginRequest.password() == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        return passwordEncoder.matches(loginRequest.password(), this.password);
    }

    public void addPremiumDetails(String username, String professionalName, String category, String aboutMe, String socialMedia, String localization, String profilePicture) {
        if (roles.stream().noneMatch(role -> role.getName().equals("PREMIUM"))) {
            throw new IllegalArgumentException("User does not have PREMIUM role");
        }

        this.username = username;
        this.professionalName = professionalName;
        this.category = category;
        this.aboutMe = aboutMe;
        this.socialMedia = socialMedia;
        this.localization = localization;
        this.profilePicture = profilePicture;

    }
}