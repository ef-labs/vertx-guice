package com.englishtown.vertx.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import io.vertx.core.json.JsonObject;

public class GuiceVertxConfigBinder extends AbstractModule {
    private static final String ROOT_PATH = "config";
    
    private final JsonObject config;
    
    public GuiceVertxConfigBinder(JsonObject config) {
        this.config = config;
    }
    
    private void bind(JsonObject config, String path) {
        bind(JsonObject.class).annotatedWith(Names.named(path)).toInstance(config);
        
        config
            .stream()
            .filter(entry -> entry.getValue() instanceof JsonObject)
            .forEach(entry -> bind((JsonObject)entry.getValue(), String.format("%s.%s", path, entry.getKey())));
    }
    
    @Override
    protected void configure() {
        bind(config, ROOT_PATH);
    }
}
