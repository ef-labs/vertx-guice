package com.englishtown.vertx.guice.integration;

import io.vertx.test.core.VertxTestBase;

import org.junit.Test;

/**
 * Integration test to show a verticle deployed with a injection constructor
 */
public class IntegrationTestVerticle extends VertxTestBase {

    @Test
    public void testDependencyInjection_Succeed() throws Exception {
        vertx.deployVerticle(DependencyInjectionVerticle.class.getName(), result -> {
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
        vertx.deployVerticle(DependencyInjectionVerticle2.class.getName(), ar -> {
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
        vertx.deployVerticle("UncompiledDIVerticle.java", result -> {
            assertTrue(result.succeeded());
            testComplete();
        });
        await();
    }

}
