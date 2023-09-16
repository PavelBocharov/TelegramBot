package com.mar.telegram.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post_type")
public class PostType {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "post_type_seq")
    private Long id;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "line", joinColumns = @JoinColumn(name = "line_id"))
    @Column(name = "lines", nullable = false)
    private List<String> lines;

}
