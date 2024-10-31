package com.proa.teiacultural.controller;

import com.proa.teiacultural.controller.dto.CreatePublicationDto;
import com.proa.teiacultural.controller.dto.FeedDto;
import com.proa.teiacultural.controller.dto.FeedItemDto;
import com.proa.teiacultural.entities.Publication;
import com.proa.teiacultural.entities.Role;
import com.proa.teiacultural.repository.PublicationRepository;
import com.proa.teiacultural.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class PublicationController {
    private final UserRepository userRepository;
    private final PublicationRepository publicationRepository;


    public PublicationController(UserRepository userRepository, PublicationRepository publicationRepository) {
        this.userRepository = userRepository;
        this.publicationRepository = publicationRepository;
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        var publications = publicationRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(publication -> new FeedItemDto(publication.getPublicationId(), publication.getContent(), publication.getUser().getEmail()));
        return ResponseEntity.ok(new FeedDto(publications.getContent(), page, pageSize, publications.getTotalPages(), publications.getTotalElements()));
    }

    @PostMapping("/publications")
    public ResponseEntity<Void> createPublication(@RequestBody CreatePublicationDto dto, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));
        // Adicionar os novos atributos aqui
        var publication = new Publication();
        publication.setUser(user.get());
        publication.setContent(dto.content());

        publicationRepository.save(publication);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/publications/{id}")
    public ResponseEntity<Void> deletePublication(@PathVariable("id") Long publicationId, JwtAuthenticationToken token) {

        var user = userRepository.findById(UUID.fromString(token.getName()));
        var publication = publicationRepository.findById(publicationId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles().stream().anyMatch((role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name())));

        if (isAdmin || publication.getUser().getId().equals(UUID.fromString(token.getName()))) {
            publicationRepository.delete(publication);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }

}
