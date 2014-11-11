package com.englishtown.vertx.guice;

import io.vertx.core.logging.impl.LogDelegate;
import io.vertx.core.logging.impl.LogDelegateFactory;

public class LogDelegateTestFactory implements LogDelegateFactory {

    TestLogDelegate delegatedLogger = new TestLogDelegate();

    @Override
    public LogDelegate createDelegate(String name) {
        return delegatedLogger;
    }
}