package com.englishtown.vertx.guice.examples;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

import javax.inject.Inject;

/**
 * Simple Guice Verticle
 */
public class ConfigVerticle extends AbstractVerticle implements Handler<Message<Void>> {

    private final ConfigDependency dependency;

    public static final String EB_ADDRESS = "et.address";

    /**
     * DI constructor
     *
     * @param dependency
     */
    @Inject
    public ConfigVerticle(ConfigDependency dependency) {
        this.dependency = dependency;
    }

    /**
     * If your verticle does a simple, synchronous start-up then override this method and put your start-up
     * code in there.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {

        if (dependency == null) {
            throw new IllegalStateException("Dependency was not injected!");
        }

        vertx.eventBus().consumer(EB_ADDRESS, this);

        super.start();
    }

    /**
     * Something has happened, so handle it.
     *
     * @param msg the event to handle
     */
    @Override
    public void handle(Message<Void> msg) {
        msg.reply(dependency.getConfig());
    }
}
