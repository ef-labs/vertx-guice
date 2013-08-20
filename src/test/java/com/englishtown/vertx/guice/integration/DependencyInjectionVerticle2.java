package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.MyDependency;
import com.englishtown.vertx.guice.MyDependency2;
import org.vertx.java.platform.Verticle;

import javax.inject.Inject;

import static org.vertx.testtools.VertxAssert.assertNotNull;

/**
 * Verticle with dependencies injected
 */
public class DependencyInjectionVerticle2 extends Verticle {

    private final MyDependency2 myDependency;

    @Inject
    public DependencyInjectionVerticle2(MyDependency2 myDependency) {
        this.myDependency = myDependency;
        assertNotNull(myDependency);
    }

}
