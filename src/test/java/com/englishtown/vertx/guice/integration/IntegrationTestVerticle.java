package com.englishtown.vertx.guice.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * Integration test to show a module deployed with a injection constructor
 */
@RunWith(CPJavaClassRunner.class)
public class IntegrationTestVerticle extends TestVerticle {

    @Test
    public void testDependencyInjection_Succeed() throws Exception {

        container.deployVerticle(DependencyInjectionVerticle.class.getName(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                    testComplete();
                } else {
                    fail(result.cause().getMessage());
                }
            }
        });

    }

    @Test
    public void testDependencyInjection_Fail() throws Exception {

        container.deployVerticle(DependencyInjectionVerticle2.class.getName(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                    fail("Should not have resolved MyDependency2");
                } else {
                    testComplete();
                }
            }
        });

    }

    @Test
    public void testDependencyInjection_Uncompiled() throws Exception {

        container.deployVerticle("UncompiledDIVerticle.java", new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                assertTrue(result.succeeded());
                testComplete();
            }
        });

    }

}
