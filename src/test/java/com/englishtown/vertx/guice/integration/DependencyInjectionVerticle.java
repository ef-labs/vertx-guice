package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.MyDependency;
import org.vertx.java.platform.Verticle;

import javax.inject.Inject;

import static org.vertx.testtools.VertxAssert.assertNotNull;

/**
 * Verticle with dependencies injected
 */
public class DependencyInjectionVerticle extends Verticle {

    private final MyDependency myDependency;

    @Inject
    public DependencyInjectionVerticle(MyDependency myDependency) {
        this.myDependency = myDependency;
        assertNotNull(myDependency);
    }

}
