package com.proa.teiacultural.controller;

import com.proa.teiacultural.controller.dto.UserDto.*;
import com.proa.teiacultural.entities.Role;
import com.proa.teiacultural.entities.User;
import com.proa.teiacultural.repository.PublicationRepository;
import com.proa.teiacultural.repository.RoleRepository;
import com.proa.teiacultural.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final PublicationRepository publicationRepository;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder, PublicationRepository publicationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.publicationRepository = publicationRepository;
    }

    // SCOPE BASIC

    @Transactional
    @PostMapping("/users")
    public ResponseEntity<Void> newUser(@RequestBody CreateUserDto dto) {
        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        var userFromDb = userRepository.findByEmail(dto.email());

        if (userFromDb.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        var user = new User();
        user.setEmail(dto.email());
        user.setPassword(bCryptPasswordEncoder.encode(dto.password()));
        user.setName(dto.name());
        user.setCpf(dto.cpf());
        user.setTelephone(dto.telephone());
        user.setRoles(Set.of(basicRole));

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<UserSummaryDto> getUserByUsername(@PathVariable String username) {
        var user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var userSummary = new UserSummaryDto(user.getId(), user.getUsername(), user.getCategory(), user.getProfessionalName());
        return ResponseEntity.ok(userSummary);
    }

    @GetMapping("/users/category/{category}")
    public ResponseEntity<List<UserSummaryDto>> getUsersByCategory(@PathVariable String category) {
        var users = userRepository.findAll()
                .stream()
                .filter(user -> user.getCategory() != null && user.getCategory().contains(category)) // Verifica se a categoria do usuário não é nula e se contém a categoria fornecida
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getCategory(), user.getProfessionalName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/profile/username/{username}")
    public ResponseEntity<UserProfileDto> getProfileByUsername(@PathVariable String username) {
        var user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var userProfile = new UserProfileDto(
                user.getUsername(),
                user.getEmail(),
                user.getTelephone(),
                user.getProfessionalName(),
                user.getCategory(),
                user.getAboutMe(),
                user.getSocialMedia(),
                user.getLocalization()
        );
        return ResponseEntity.ok(userProfile);
    }

    // SCOPE PREMIUM

    @Transactional
    @PostMapping("/users/upgrade-to-premium")
    public ResponseEntity<Void> upgradeToPremiumAuthenticatedUser(Authentication authentication, @RequestBody UpgradeToPremiumDto dto) {
        var userId = UUID.fromString(authentication.getName());
        var user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("admin"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot upgrade admin user");
        }

        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        var premiumRole = roleRepository.findByName(Role.Values.PREMIUM.name());
        if (premiumRole == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Premium role not found");
        }

        // Verificar se o username já existe
        var existingUser = userRepository.findByUsername(dto.username());
        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        // Remover a role BASIC e adicionar a role PREMIUM
        user.getRoles().remove(basicRole);
        user.getRoles().add(premiumRole);
        user.setUsername(dto.username());
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @PostMapping("/users/downgrade-to-basic")
    public ResponseEntity<Void> downgradeToBasicAuthenticatedUser(Authentication authentication) {
        var userId = UUID.fromString(authentication.getName());
        var user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("admin"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot downgrade admin user");
        }

        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        var premiumRole = roleRepository.findByName(Role.Values.PREMIUM.name());
        if (basicRole == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Basic role not found");
        }

        // Remover a role PREMIUM e adicionar a role BASIC
        user.getRoles().remove(premiumRole);
        user.getRoles().add(basicRole);
        user.setUsername(null);

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @PatchMapping("/users/add-premium-details")
    public ResponseEntity<Void> updatePremiumDetailsAuthenticatedUser(Authentication authentication, @RequestBody UpdatePremiumDetailsDto dto) {
        var userId = UUID.fromString(authentication.getName());
        var user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getRoles().stream().noneMatch(role -> role.getName().equals("premium"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have PREMIUM role");
        }


        if (dto.professionalName() != null) {
            user.setProfessionalName(dto.professionalName());
        }
        if (dto.category() != null) {
            user.setCategory(dto.category());
        }
        if (dto.aboutMe() != null) {
            user.setAboutMe(dto.aboutMe());
        }
        if (dto.socialMedia() != null) {
            user.setSocialMedia(dto.socialMedia());
        }
        if (dto.localization() != null) {
            user.setLocalization(dto.localization());
        }
        if (dto.profilePicture() != null) {
            user.setProfilePicture(dto.profilePicture());
        }

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    // SCOPE ADMIN

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public ResponseEntity<List<User>> listUsers() {
        var users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @Transactional
    @PostMapping("/users/{id}/upgrade-to-premium")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public ResponseEntity<Void> upgradeToPremium(@PathVariable UUID id, @RequestBody UpgradeToPremiumDto dto) {
        var user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("admin"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot upgrade admin user");
        }

        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        var premiumRole = roleRepository.findByName(Role.Values.PREMIUM.name());
        if (premiumRole == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Premium role not found");
        }

        // Verificar se o username já existe
        var existingUser = userRepository.findByUsername(dto.username());
        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        // Remover a role BASIC e adicionar a role PREMIUM
        user.getRoles().remove(basicRole);
        user.getRoles().add(premiumRole);
        user.setUsername(dto.username());
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @PostMapping("/users/{id}/downgrade-to-basic")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public ResponseEntity<Void> downgradeToBasic(@PathVariable UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("admin"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot downgrade admin user");
        }

        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        var premiumRole = roleRepository.findByName(Role.Values.PREMIUM.name());
        if (basicRole == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Basic role not found");
        }

        // Remover a role PREMIUM e adicionar a role BASIC
        user.getRoles().remove(premiumRole);
        user.getRoles().add(basicRole);
        user.setUsername(null);

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @PatchMapping("/users/{id}/add-premium-details")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public ResponseEntity<Void> addPremiumDetails(@PathVariable UUID id, @RequestBody CreatePremiumDetailsDto dto) {
        var user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (dto.professionalName() != null) {
            user.setProfessionalName(dto.professionalName());
        }
        if (dto.category() != null) {
            user.setCategory(dto.category());
        }
        if (dto.aboutMe() != null) {
            user.setAboutMe(dto.aboutMe());
        }
        if (dto.socialMedia() != null) {
            user.setSocialMedia(dto.socialMedia());
        }
        if (dto.localization() != null) {
            user.setLocalization(dto.localization());
        }

        if (dto.profilePicture() != null) {
            user.setProfilePicture(dto.profilePicture());
        }

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @DeleteMapping("/users/delete/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Verifica se o usuário a ser deletado é um administrador
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("admin"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete admin user");
        }

        // Deletar todas as publicações relacionadas ao usuário
        publicationRepository.deleteByUser(user);

        // Remover as associações na tabela intermediária tb_users_roles
        user.getRoles().clear();
        userRepository.save(user);

        // Deletar o usuário
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}