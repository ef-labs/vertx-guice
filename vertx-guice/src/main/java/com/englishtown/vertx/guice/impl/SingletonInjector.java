package com.englishtown.vertx.guice.impl;

import com.englishtown.vertx.guice.InjectorBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.List;

/**
 * Created by jensklingsporn on 20.10.15.
 */
public class SingletonInjector implements InjectorBuilder {

    private static Injector injector;

    @Override
    public Injector create(List<Module> modules, String realVerticleName) {
        if(injector == null){
            synchronized (getClass()){
                if(injector == null){
                    injector = Guice.createInjector(modules);
                }
            }
        }
        return injector;
    }
}