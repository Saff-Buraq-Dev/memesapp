package com.memevote.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "memes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "votes", "comments", "categories"})
@EqualsAndHashCode(of = {"id", "title", "url"})
public class Meme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    private String url;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "meme", cascade = CascadeType.ALL)
    private Set<Vote> votes = new HashSet<>();

    @OneToMany(mappedBy = "meme", cascade = CascadeType.ALL)
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "meme_categories",
        joinColumns = @JoinColumn(name = "meme_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @org.hibernate.annotations.Formula("(SELECT COUNT(*) FROM votes v WHERE v.meme_id = id)")
    private Long voteCount;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
