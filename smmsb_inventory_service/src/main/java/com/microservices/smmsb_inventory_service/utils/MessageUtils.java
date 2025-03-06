package com.microservices.smmsb_inventory_service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageUtils {
   private final MessageSource messageSource;

    @Autowired
    public MessageUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}