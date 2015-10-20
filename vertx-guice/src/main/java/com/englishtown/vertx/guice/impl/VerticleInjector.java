package com.englishtown.vertx.guice.impl;

import com.englishtown.vertx.guice.InjectorBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.List;

/**
 * Created by jensklingsporn on 20.10.15.
 */
public class VerticleInjector implements InjectorBuilder {

    @Override
    public Injector create(List<Module> modules, String realVerticleName) {
        return Guice.createInjector(modules);
    }
}
