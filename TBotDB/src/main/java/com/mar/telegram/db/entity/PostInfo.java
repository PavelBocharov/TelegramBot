package com.mar.telegram.db.entity;

import com.mar.dto.tbot.ContentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "post_info")
public class PostInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rand_task_seq")  // ???? 'rand_task_seq' WTF ????
    private Long id;

    @Column(name = "media_path")
    private String mediaPath;

    @Column(name = "caption", length = 1024)
    private String caption;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "is_send", nullable = false)
    private Boolean isSend = false;

    @Column(name = "media_type")
    private ContentType type;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "create_date", updatable = false, columnDefinition = "DATE DEFAULT NOW()")
    @CreationTimestamp
    private Date createDate;

    @Column(name = "schedule_send_flag", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean sendFlag;

    @Column(name = "schedule_send_time")
    private Date scheduleSendTime;

}
