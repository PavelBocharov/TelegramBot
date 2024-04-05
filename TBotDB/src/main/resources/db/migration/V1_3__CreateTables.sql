CREATE TABLE IF NOT EXISTS hashtag
(
    id bigint NOT NULL,
    tag character varying(255),
    CONSTRAINT hashtag_pk PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_info
(
    id bigint NOT NULL,
    user_id bigint,
    CONSTRAINT user_info_pk PRIMARY KEY (id),
    CONSTRAINT uk_user_info_user_id UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS post_type
(
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    CONSTRAINT post_type_pk PRIMARY KEY (id),
    CONSTRAINT uk_post_type_title UNIQUE (title)
);

CREATE TABLE IF NOT EXISTS line
(
    line_id bigint NOT NULL,
    lines character varying(255) NOT NULL,
    CONSTRAINT line_post_type_fk FOREIGN KEY (line_id)
        REFERENCES post_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS post_info
(
    id bigint NOT NULL,
    caption character varying(1024),
    chat_id bigint,
    create_date date DEFAULT now(),
    is_send boolean NOT NULL,
    media_path character varying(255),
    message_id integer,
    media_type smallint,
    update_date timestamp(6) without time zone,
    schedule_send_time timestamp(6) without time zone,
    schedule_send_flag boolean NOT NULL DEFAULT true,
    CONSTRAINT post_info_pk PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS action_post
(
    id bigint NOT NULL,
    action smallint NOT NULL,
    create_date date DEFAULT now(),
    update_date timestamp(6) without time zone,
    post_id bigint,
    user_info_id bigint NOT NULL,
    CONSTRAINT action_post_pk PRIMARY KEY (id),
    CONSTRAINT action_user_info_fk FOREIGN KEY (user_info_id)
        REFERENCES user_info (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT action_post_info_fk FOREIGN KEY (post_id)
        REFERENCES post_info (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);