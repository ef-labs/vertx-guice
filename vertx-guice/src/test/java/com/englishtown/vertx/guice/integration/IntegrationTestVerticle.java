package com.englishtown.vertx.guice.integration;

import com.englishtown.vertx.guice.*;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Integration test to show a verticle deployed with a injection constructor
 */
public class IntegrationTestVerticle extends VertxTestBase {

    @Test
    public void testDependencyInjection_Succeed() throws Exception {
        deployVerticle(DependencyInjectionVerticle.class).get();
    }

    @Test(expected = ExecutionException.class)
    public void testDependencyInjection_Fail() throws Exception {
        deployVerticle(DependencyInjectionVerticle2.class).get();
    }

    @Test
    public void testDependencyInjection_ParentInjector() throws Exception {

        Injector parent = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MyDependency2.class).to(DefaultMyDependency2.class).in(Singleton.class);
            }
        });

        getFactory().setInjector(parent);
        deployVerticle(DependencyInjectionVerticle2.class).get();

    }

    @Test
    public void testDependencyInjection_ParentInjector_Vertx() throws Exception {

        Injector parent = Guice.createInjector(new GuiceVertxBinder(vertx), new AbstractModule() {
            @Override
            protected void configure() {
                bind(MyDependency.class).to(CustomMyDependency.class).in(Singleton.class);
            }
        });

        getFactory().setInjector(parent);

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put(GuiceVerticleLoader.CONFIG_BOOTSTRAP_BINDER_NAME, NOPBinder.class.getName()));

        deployVerticle(DependencyInjectionVerticle.class, options).get();

        vertx.eventBus()
                .<String>send(DependencyInjectionVerticle.EB_ADDRESS, null, result -> {
                    if (result.succeeded()) {
                        assertEquals(CustomMyDependency.class.getName(), result.result().body());
                        testComplete();
                    } else {
                        fail(result.cause());
                    }
                });

        await();
    }


    @Test
    public void testDependencyInjection_PromiseStartMethod() throws Exception {

        Injector parent = Guice.createInjector(new GuiceVertxBinder(vertx), new AbstractModule() {
            @Override
            protected void configure() {
                bind(MyDependency.class).to(CustomMyDependency.class).in(Singleton.class);
            }
        });

        getFactory().setInjector(parent);

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put(GuiceVerticleLoader.CONFIG_BOOTSTRAP_BINDER_NAME, NOPBinder.class.getName()));

        deployVerticle(DependencyInjectionVerticle3.class, options).get();

        vertx.eventBus()
                .<String>send(DependencyInjectionVerticle3.EB_ADDRESS, null, result -> {
                    if (result.succeeded()) {
                        assertEquals(CustomMyDependency.class.getName(), result.result().body());
                        testComplete();
                    } else {
                        fail(result.cause());
                    }
                });

        await();
    }

    @Test
    public void testDependencyInjection_Uncompiled() throws Exception {
        deployVerticle("UncompiledDIVerticle.java").get();
    }

    private GuiceVerticleFactory getFactory() {
        return vertx.verticleFactories()
                .stream()
                .filter(f -> f instanceof GuiceVerticleFactory)
                .map(f -> (GuiceVerticleFactory) f)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find the guice verticle factory..."));
    }

    private CompletableFuture<String> deployVerticle(Class<? extends Verticle> clazz) {
        return deployVerticle(clazz, new DeploymentOptions());
    }

    private CompletableFuture<String> deployVerticle(Class<? extends Verticle> clazz, DeploymentOptions options) {
        return deployVerticle(clazz.getName(), options);
    }

    private CompletableFuture<String> deployVerticle(String name) {
        return deployVerticle(name, new DeploymentOptions());
    }

    private CompletableFuture<String> deployVerticle(String name, DeploymentOptions options) {
        CompletableFuture<String> future = new CompletableFuture<>();

        vertx.deployVerticle(GuiceVerticleFactory.PREFIX + ":" + name, options, result -> {
            if (result.succeeded()) {
                future.complete(result.result());
            } else {
                future.completeExceptionally(result.cause());
            }
        });

        return future;
    }

    private static class CustomMyDependency extends DefaultMyDependency {

        @Inject
        public CustomMyDependency(Vertx vertx) {
            super(vertx);
        }

    }

}
