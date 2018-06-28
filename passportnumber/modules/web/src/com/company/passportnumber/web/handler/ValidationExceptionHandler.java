package com.company.passportnumber.web.handler;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.exception.AbstractGenericExceptionHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.ValidationException;

@Component("passportnumber_ValidationExceptionHandler")
public class ValidationExceptionHandler extends AbstractGenericExceptionHandler {

    public ValidationExceptionHandler() {
        super(ValidationException.class.getName());
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable, WindowManager windowManager) {
        windowManager.showNotification(message, Frame.NotificationType.WARNING);
    }
}
