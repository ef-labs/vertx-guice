package com.englishtown.vertx.guice.integration;

import static org.junit.Assert.assertNotNull;
import io.vertx.core.AbstractVerticle;

import javax.inject.Inject;

import com.englishtown.vertx.guice.MyDependency;

/**
 * Verticle with dependencies injected
 */
public class DependencyInjectionVerticle extends AbstractVerticle {

    private final MyDependency myDependency;

    @Inject
    public DependencyInjectionVerticle(MyDependency myDependency) {
        this.myDependency = myDependency;
        assertNotNull(myDependency);
    }

}
