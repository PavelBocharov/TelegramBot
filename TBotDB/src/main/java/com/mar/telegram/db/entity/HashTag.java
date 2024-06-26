package com.mar.telegram.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hashtag")
public class HashTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hashtag_gen")
    @SequenceGenerator(name = "hashtag_gen", sequenceName = "hashtag_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tag")
    private String tag;

}
