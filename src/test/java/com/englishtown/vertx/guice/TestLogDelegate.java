package com.englishtown.vertx.guice;

import io.vertx.core.logging.impl.LogDelegate;

class TestLogDelegate implements LogDelegate {

    private static String lastError = "";
    private static boolean wasUsed = false;

    /**
     * Check whether the logger was ever used by any of the logging methods (eg. fatal, error, debug, info)
     * 
     * @return
     */
    public static boolean wasUsed() {
        return wasUsed;
    }

    /**
     * Return the last error that has been logged.
     * 
     * @return
     */
    public static String getLastError() {
        return lastError;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void fatal(Object message) {
        wasUsed = true;
    }

    @Override
    public void fatal(Object message, Throwable t) {
        wasUsed = true;
    }

    @Override
    public void error(Object message) {
        lastError = (String) message;
        wasUsed = true;
    }

    @Override
    public void error(Object message, Throwable t) {
        lastError = (String) message;
        wasUsed = true;
    }

    @Override
    public void warn(Object message) {
        wasUsed = true;
    }

    @Override
    public void warn(Object message, Throwable t) {
        wasUsed = true;

    }

    @Override
    public void info(Object message) {
        wasUsed = true;
    }

    @Override
    public void info(Object message, Throwable t) {
        wasUsed = true;
    }

    @Override
    public void debug(Object message) {
        wasUsed = true;
    }

    @Override
    public void debug(Object message, Throwable t) {
        wasUsed = true;
    }

    @Override
    public void trace(Object message) {
        wasUsed = true;
    }

    @Override
    public void trace(Object message, Throwable t) {
        wasUsed = true;
    }

    public static void reset() {
        lastError = "";
        wasUsed = false;
    }

}
