package com.englishtown.vertx.guice.integration;

import static org.junit.Assert.assertNotNull;
import io.vertx.core.AbstractVerticle;

import javax.inject.Inject;

import com.englishtown.vertx.guice.MyDependency2;

/**
 * Verticle with dependencies injected
 */
public class DependencyInjectionVerticle2 extends AbstractVerticle {

    @SuppressWarnings("unused")
    private final MyDependency2 myDependency;

    @Inject
    public DependencyInjectionVerticle2(MyDependency2 myDependency) {
        this.myDependency = myDependency;
        assertNotNull(myDependency);
    }

}
