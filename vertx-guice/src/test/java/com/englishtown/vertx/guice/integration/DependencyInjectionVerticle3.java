package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.MyDependency;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

public class DependencyInjectionVerticle3 extends AbstractVerticle {

    public static final String EB_ADDRESS = "et.address";
    private final MyDependency myDependency;

    @Inject
    public DependencyInjectionVerticle3(MyDependency myDependency) {
        this.myDependency = myDependency;
        assertNotNull(myDependency);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.eventBus()
                .<Void>consumer(EB_ADDRESS)
                .handler(msg -> msg.reply(myDependency.getClass().getName()));

        startPromise.complete();
    }
}
