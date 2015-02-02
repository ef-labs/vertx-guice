package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.GuiceVerticleFactory;
import io.vertx.test.core.VertxTestBase;

import org.junit.Test;

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
        vertx.deployVerticle(identifier, ar -> {
            if (ar.succeeded()) {
                fail("Should not have resolved MyDependency2");
            } else {
                testComplete();
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
