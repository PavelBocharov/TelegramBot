package org.mar.telegram.bot.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "action_post")
public class ActionPost {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "action_post_seq")
    private Long id;

    @OneToOne()
    private PostInfo post;

    @Column(name = "action")
    private ActionEnum action;

    @ManyToOne
    @JoinColumn(name="user_info_id", nullable=true)
    private UserInfo userInfo;

}
