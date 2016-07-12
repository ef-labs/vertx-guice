package com.englishtown.vertx.guice.examples;

import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Dependency using named annotations to inject config
 */
public class ConfigDependency {

    private JsonObject config;

    @Inject
    public ConfigDependency(@Named("config.api") JsonObject config) {
        this.config = config;
    }

    public JsonObject getConfig() {
        return config;
    }
}
