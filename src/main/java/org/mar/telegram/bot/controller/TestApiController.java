package org.mar.telegram.bot.controller;


import lombok.extern.slf4j.Slf4j;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestApiController {

    @Autowired
    private MQSender<LoadFileInfo> mqSender;

    @PostMapping(
            value = "/test",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LoadFileInfo sendMsg(@RequestBody LoadFileInfo msg) {
        log.warn("REST API: {}", msg);
        mqSender.send(msg);
        return msg;
    }

}
