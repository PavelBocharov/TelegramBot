package com.mar.telegram.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "action_post")
public class ActionPost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "action_post_gen")
    @SequenceGenerator(name = "action_post_gen", sequenceName = "action_post_seq", allocationSize = 1)
    private Long id;

    @OneToOne
    private PostInfo post;

    @Column(name = "action", nullable = false)
    private ActionEnum action;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "create_date", updatable = false, columnDefinition = "DATE DEFAULT NOW()")
    @CreationTimestamp
    private Date createDate;

    @ManyToOne
    @JoinColumn(name = "user_info_id", nullable = false)
    private UserInfo userInfo;

}
