package com.mar.telegram.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hashtag")
public class HashTag {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hashtag_seq")
    private Long id;

    @Column(name = "tag")
    private String tag;

}
