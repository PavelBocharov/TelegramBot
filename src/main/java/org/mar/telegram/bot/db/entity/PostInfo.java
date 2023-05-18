package org.mar.telegram.bot.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mar.telegram.bot.utils.ContentType;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post_info")
public class PostInfo {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "rand_task_seq")
    private Long id;

    @Column(name = "media_path")
    private String mediaPath;

    @Column(name = "caption")
    private String caption;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "is_send", nullable = false)
    private Boolean isSend = false;

    @Column(name = "media_type")
    private ContentType type;

}
