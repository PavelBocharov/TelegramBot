package org.mar.telegram.bot.service.jms;

public interface MQSender<T> {
    void send(T sendFile);
}
