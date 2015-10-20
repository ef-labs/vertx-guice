package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.MyDependency;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.impl.ConcurrentHashSet;

import javax.inject.Inject;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * Verticle with dependencies injected
 */
public class DependencyInjectionVerticle3 extends AbstractVerticle {

    public static Set<MyDependency> dependencies = new ConcurrentHashSet<>();
    private final MyDependency myDependency;

    @Inject
    public DependencyInjectionVerticle3(MyDependency myDependency) {
        this.myDependency = myDependency;
        assertNotNull(myDependency);
        dependencies.add(myDependency);
    }

}
