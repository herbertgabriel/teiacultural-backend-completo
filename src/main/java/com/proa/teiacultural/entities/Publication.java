package com.proa.teiacultural.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tb_publication")
@Getter
@Setter
public class Publication {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "publication_id")
    private long publicationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String content;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;

    @CreationTimestamp
    private Instant creationTimestamp;
}