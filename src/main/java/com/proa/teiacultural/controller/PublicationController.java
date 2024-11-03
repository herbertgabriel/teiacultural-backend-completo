package com.proa.teiacultural.controller;

import com.proa.teiacultural.controller.dto.FeedDto.*;
import com.proa.teiacultural.controller.dto.PublicationDto.CreatePublicationDto;
import com.proa.teiacultural.controller.dto.PublicationDto.UpdatePublicationDto;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        var publications = publicationRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(publication -> new FeedItemDto(
                        publication.getPublicationId(),
                        publication.getUser().getProfessionalName(),
                        publication.getUser().getProfilePicture(),
                        publication.getUser().getUsername(),
                        publication.getUser().getCategory(),
                        publication.getContent(),
                        publication.getImageUrl1(),
                        publication.getImageUrl2(),
                        publication.getImageUrl3(),
                        publication.getImageUrl4()
                ));
        return ResponseEntity.ok(new FeedDto(publications.getContent(), page, pageSize, publications.getTotalPages(), publications.getTotalElements()));
    }

    @GetMapping("/feed/filter/username/{username}")
    public ResponseEntity<FilteredFeedDto> filterByUsername(
            @PathVariable String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        var user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var publicationsPage = publicationRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"));

        var filteredPublications = publicationsPage.getContent().stream()
                .filter(publication -> username.equals(publication.getUser().getUsername()))
                .map(publication -> new FeedFilterDto(
                        publication.getPublicationId(),
                        publication.getUser().getProfessionalName(),
                        publication.getUser().getProfilePicture(),
                        publication.getUser().getUsername(),
                        publication.getUser().getCategory(),
                        publication.getContent(),
                        publication.getImageUrl1(),
                        publication.getImageUrl2(),
                        publication.getImageUrl3(),
                        publication.getImageUrl4()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new FilteredFeedDto(filteredPublications, page, pageSize, publicationsPage.getTotalPages(), publicationsPage.getTotalElements()));
    }

    @GetMapping("/feed/filter/category/{category}")
    public ResponseEntity<FilteredFeedDto> filterByCategory(
            @PathVariable String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        var users = userRepository.findAll()
                .stream()
                .filter(user -> user.getCategory() != null && user.getCategory().contains(category))
                .collect(Collectors.toList());

        var publicationsPage = publicationRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"));

        var filteredPublications = publicationsPage.getContent().stream()
                .filter(publication -> users.contains(publication.getUser()))
                .map(publication -> new FeedFilterDto(
                        publication.getPublicationId(),
                        publication.getUser().getProfessionalName(),
                        publication.getUser().getProfilePicture(),
                        publication.getUser().getUsername(),
                        publication.getUser().getCategory(),
                        publication.getContent(),
                        publication.getImageUrl1(),
                        publication.getImageUrl2(),
                        publication.getImageUrl3(),
                        publication.getImageUrl4()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new FilteredFeedDto(filteredPublications, page, pageSize, publicationsPage.getTotalPages(), publicationsPage.getTotalElements()));
    }


    @GetMapping("/feed/username/{username}")
    public ResponseEntity<List<FeedFilterDto>> usernameFeed(@PathVariable String username) {
        var users = userRepository.findAll()
                .stream()
                .filter(user -> username.equals(user.getUsername()))
                .collect(Collectors.toList());

        var publications = publicationRepository.findAll()
                .stream()
                .filter(publication -> users.contains(publication.getUser()))
                .map(publication -> new FeedFilterDto(
                        publication.getPublicationId(),
                        publication.getUser().getProfessionalName(),
                        publication.getUser().getProfilePicture(),
                        publication.getUser().getUsername(),
                        publication.getUser().getCategory(),
                        publication.getContent(),
                        publication.getImageUrl1(),
                        publication.getImageUrl2(),
                        publication.getImageUrl3(),
                        publication.getImageUrl4()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(publications);
    }


    @GetMapping("/feed/category/{category}")
    public ResponseEntity<List<FeedFilterDto>> categoryFeed(@PathVariable String category) {
        var users = userRepository.findAll()
                .stream()
                .filter(user -> user.getCategory() != null && user.getCategory().contains(category))
                .collect(Collectors.toList());

        var publications = publicationRepository.findAll()
                .stream()
                .filter(publication -> users.contains(publication.getUser()))
                .map(publication -> new FeedFilterDto(
                        publication.getPublicationId(),
                        publication.getUser().getProfessionalName(),
                        publication.getUser().getProfilePicture(),
                        publication.getUser().getUsername(),
                        publication.getUser().getCategory(),
                        publication.getContent(),
                        publication.getImageUrl1(),
                        publication.getImageUrl2(),
                        publication.getImageUrl3(),
                        publication.getImageUrl4()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(publications);
    }

    @GetMapping("/profile/publications/{username}")
    public ResponseEntity<List<FeedProfileDto>> profileFeed(@PathVariable String username) {
        var user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var publications = publicationRepository.findAll()
                .stream()
                .map(publication -> new FeedProfileDto(
                        publication.getPublicationId(),
                        publication.getContent(),
                        publication.getImageUrl1(),
                        publication.getImageUrl2(),
                        publication.getImageUrl3(),
                        publication.getImageUrl4()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(publications);
    }


     @PostMapping("/publications")
    public ResponseEntity<Void> createPublication(@RequestBody CreatePublicationDto dto, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getRoles().stream().noneMatch(role -> role.getName().equals("premium"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have PREMIUM role");
        }

        var publication = new Publication();
        publication.setUser(user);
        publication.setContent(dto.content());
        publication.setImageUrl1(dto.imageUrl1());
        publication.setImageUrl2(dto.imageUrl2());
        publication.setImageUrl3(dto.imageUrl3());
        publication.setImageUrl4(dto.imageUrl4());

        publicationRepository.save(publication);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/publications/{id}")
    public ResponseEntity<Void> patchPublication(@PathVariable Long id, @RequestBody UpdatePublicationDto dto, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getRoles().stream().noneMatch(role -> role.getName().equals("premium"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have PREMIUM role");
        }

        var publication = publicationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!publication.getUser().equals(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not own this publication");
        }

        if (dto.content() != null) {
            publication.setContent(dto.content());
        }
        if (dto.imageUrl1() != null) {
            publication.setImageUrl1(dto.imageUrl1());
        }
        if (dto.imageUrl2() != null) {
            publication.setImageUrl2(dto.imageUrl2());
        }
        if (dto.imageUrl3() != null) {
            publication.setImageUrl3(dto.imageUrl3());
        }
        if (dto.imageUrl4() != null) {
            publication.setImageUrl4(dto.imageUrl4());
        }

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
