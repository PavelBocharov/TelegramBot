package org.mar.telegram.bot.controller;

import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TestApiController {

    @Autowired
    private MQSender mqSender;

    @PostMapping(
            value = "/test",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LoadFileInfo sendMsg(@RequestBody LoadFileInfo msg) {
        String rqUuid = UUID.randomUUID().toString();
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "REST API: {}", msg);
        mqSender.sendFileInfo(rqUuid, msg);
        return msg;
    }

}
