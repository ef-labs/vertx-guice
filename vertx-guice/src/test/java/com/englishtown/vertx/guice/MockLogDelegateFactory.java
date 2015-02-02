package com.englishtown.vertx.guice;

import io.vertx.core.logging.impl.LogDelegate;
import io.vertx.core.logging.impl.LogDelegateFactory;
import io.vertx.core.logging.impl.LoggerFactory;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

public class MockLogDelegateFactory implements LogDelegateFactory {

    private static LogDelegate logDelegate = mock(LogDelegate.class);

    static {
        // Use our own test logger factory / logger instead. We can't use powermock to statically mock the
        // LoggerFactory since javassist 1.18.x contains a bug that prevents the usage of powermock.
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, MockLogDelegateFactory.class.getName());
        LoggerFactory.removeLogger(GuiceVerticleLoader.class.getName());
        LoggerFactory.initialise();
    }

    public static LogDelegate getLogDelegate() {
        return logDelegate;
    }

    public static void reset() {
        Mockito.reset(logDelegate);
    }

    @Override
    public LogDelegate createDelegate(String name) {
        return logDelegate;
    }
}