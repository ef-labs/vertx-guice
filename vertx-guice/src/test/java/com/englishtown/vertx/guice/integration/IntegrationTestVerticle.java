package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.GuiceVerticleFactory;
import com.englishtown.vertx.guice.GuiceVerticleLoader;
import com.englishtown.vertx.guice.impl.SingletonInjector;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
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

    @Test
    public void testDependencyInjection_VerticleInjector() throws Exception {

        String identifier = GuiceVerticleFactory.PREFIX + ":" + DependencyInjectionVerticle3.class.getName();
        DependencyInjectionVerticle3.dependencies.clear();
        vertx.deployVerticle(identifier, new DeploymentOptions().setConfig(new JsonObject().put("guice_binder",CustomSingletonBinder.class.getName())).setInstances(2), result -> {
            if (result.succeeded()) {
                /*
                 * Because we create an Injector per Verticle, we'll have two 'singleton'-dependencies
                 */
                assertEquals(2,DependencyInjectionVerticle3.dependencies.size());
                testComplete();
            } else {
                fail(result.cause().getMessage());
            }
        });
        await();
    }

    @Test
    public void testDependencyInjection_SingletonInjector() throws Exception {

        String identifier = GuiceVerticleFactory.PREFIX + ":" + DependencyInjectionVerticle3.class.getName();
        DependencyInjectionVerticle3.dependencies.clear();
        vertx.deployVerticle(identifier, new DeploymentOptions().setConfig(new JsonObject().put("guice_binder",CustomSingletonBinder.class.getName()).put(GuiceVerticleLoader.CONFIG_BOOTSTRAP_INJECTOR_CLASS_NAME, SingletonInjector.class.getName())).setInstances(2), result -> {
            if (result.succeeded()) {
                /*
                 * Because we create one Injector for all Verticles, we'll have only one 'singleton'-dependency
                 */
                assertEquals(1,DependencyInjectionVerticle3.dependencies.size());
                testComplete();
            } else {
                fail(result.cause().getMessage());
            }
        });
        await();
    }


}
