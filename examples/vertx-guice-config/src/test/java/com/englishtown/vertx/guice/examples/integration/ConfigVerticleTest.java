package com.englishtown.vertx.guice.examples.integration;

import com.englishtown.vertx.guice.examples.ConfigBinder;
import com.englishtown.vertx.guice.examples.ConfigVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Integration test for {@link ConfigVerticle}
 */
public class ConfigVerticleTest extends VertxTestBase {

    private JsonObject apiConfig;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        CompletableFuture<Void> future = new CompletableFuture<>();
        apiConfig = new JsonObject()
                .put("a", "b")
                .put("b", 123)
                .put("c", true);

        JsonObject config = new JsonObject()
                .put("guice_binder", ConfigBinder.class.getName())
                .put(ConfigBinder.GUICE_CONFIG, new JsonObject()
                        .put("api", apiConfig));

        vertx.deployVerticle("java-guice:" + ConfigVerticle.class.getName(), new DeploymentOptions().setConfig(config), result -> {
            if (result.succeeded()) {
                future.complete(null);
            } else {
                future.completeExceptionally(result.cause());
            }
        });

        future.get(200, TimeUnit.SECONDS);

    }

    @Test
    public void testHandle() throws Exception {

        vertx.eventBus().<JsonObject>send(ConfigVerticle.EB_ADDRESS, null, result -> {

            if (result.failed()) {
                result.cause().printStackTrace();
                fail();
                return;
            }

            assertEquals(apiConfig, result.result().body());
            testComplete();

        });

        await();
    }
}