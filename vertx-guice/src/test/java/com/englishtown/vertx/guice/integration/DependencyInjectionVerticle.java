package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.MyDependency;
import io.vertx.core.AbstractVerticle;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

/**
 * Verticle with dependencies injected
 */
public class DependencyInjectionVerticle extends AbstractVerticle {

    public static final String EB_ADDRESS = "et.address";

    private final MyDependency myDependency;

    @Inject
    public DependencyInjectionVerticle(MyDependency myDependency) {
        this.myDependency = myDependency;
        assertNotNull(myDependency);
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus()
                .<Void>consumer(EB_ADDRESS)
                .handler(msg -> msg.reply(myDependency.getClass().getName()));
    }

}
