package com.englishtown.vertx.guice.examples;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Guice config module
 */
public class ConfigBinder extends AbstractModule {

    public static final String GUICE_CONFIG = "guice_config";
    public static final String ROOT_PATH = "config";

    @Override
    protected void configure() {

        JsonObject config = Vertx.currentContext().config().getJsonObject(GUICE_CONFIG);
        if (config != null) {
            bindConfig(config, ROOT_PATH);
        }

    }

    private void bindConfig(JsonObject config, String path) {
        bind(JsonObject.class).annotatedWith(Names.named(path)).toInstance(config);

        config
                .stream()
                .filter(entry -> entry.getValue() instanceof JsonObject)
                .forEach(entry -> bindConfig((JsonObject) entry.getValue(), String.format("%s.%s", path, entry.getKey())));
    }
}
