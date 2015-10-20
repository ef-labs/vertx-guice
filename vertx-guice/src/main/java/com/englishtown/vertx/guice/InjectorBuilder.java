package com.englishtown.vertx.guice;

import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.List;

/**
 * Created by jensklingsporn on 19.10.15.
 */
public interface InjectorBuilder {

    /**
     *
     * @param modules the modules to create the Injector with
     * @param realVerticleName the name of the real verticle to load
     * @return the Injector
     */
    Injector create(List<Module> modules, String realVerticleName);
}
