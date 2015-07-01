package com.englishtown.vertx.guice.examples;

import com.englishtown.vertx.guice.examples.impl.MyDependencyImpl;
import com.google.inject.AbstractModule;

import javax.inject.Singleton;

/**
 * Guice module
 */
public class Binder extends AbstractModule {
    /**
     * Configures a {@link Binder} via the exposed methods.
     */
    @Override
    protected void configure() {
        bind(MyDependency.class).to(MyDependencyImpl.class).in(Singleton.class);
    }
}
