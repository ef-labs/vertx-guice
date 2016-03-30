package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.DefaultMyDependency2;
import com.englishtown.vertx.guice.GuiceVerticleFactory;
import com.englishtown.vertx.guice.MyDependency2;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import javax.inject.Singleton;

/**
 * Integration test to show a verticle deployed with a injection constructor
 */
public class IntegrationTestVerticle extends VertxTestBase {

    @Test
    public void testDependencyInjection_Succeed() throws Exception {
        String identifier = GuiceVerticleFactory.PREFIX + ":" + DependencyInjectionVerticle.class.getName();
        vertx.deployVerticle(identifier, result -> {
            if (result.succeeded()) {
                testComplete();
            } else {
                fail(result.cause().getMessage());
            }
        });
        await();
    }

    @Test
    public void testDependencyInjection_Fail() throws Exception {
        String identifier = GuiceVerticleFactory.PREFIX + ":" + DependencyInjectionVerticle2.class.getName();
        vertx.deployVerticle(identifier, result -> {
            if (result.succeeded()) {
                fail("Should not have resolved MyDependency2");
            } else {
                testComplete();
            }
        });
        await();
    }

    @Test
    public void testDependencyInjection_ParentInjector() throws Exception {

        GuiceVerticleFactory factory = vertx.verticleFactories()
                .stream()
                .filter(f -> f instanceof GuiceVerticleFactory)
                .map(f -> (GuiceVerticleFactory) f)
                .findFirst()
                .get();

        factory.setInjector(Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MyDependency2.class).to(DefaultMyDependency2.class).in(Singleton.class);
            }
        }));

        String identifier = GuiceVerticleFactory.PREFIX + ":" + DependencyInjectionVerticle2.class.getName();
        vertx.deployVerticle(identifier, result -> {
            if (result.succeeded()) {
                testComplete();
            } else {
                fail(result.cause().getMessage());
            }
        });
        await();
    }

    @Test
    public void testDependencyInjection_Uncompiled() throws Exception {
        String identifier = GuiceVerticleFactory.PREFIX + ":" + "UncompiledDIVerticle.java";
        vertx.deployVerticle(identifier, result -> {
            assertTrue(result.succeeded());
            testComplete();
        });
        await();
    }

}
