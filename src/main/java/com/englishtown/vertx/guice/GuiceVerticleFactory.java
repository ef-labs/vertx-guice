/*
 * The MIT License (MIT)
 * Copyright © 2013 Englishtown <opensource@englishtown.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.englishtown.vertx.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.impl.java.CompilingClassLoader;
import org.vertx.java.platform.impl.java.JavaVerticleFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends the default vert.x {@link JavaVerticleFactory} using Guice for dependency injection.
 */
public class GuiceVerticleFactory extends JavaVerticleFactory {

    private Vertx vertx;
    private Container container;
    private ClassLoader cl;

    private static final Logger logger = LoggerFactory.getLogger(GuiceVerticleFactory.class);

    private static final String BOOTSTRAP_BINDER_NAME = "com.englishtown.vertx.guice.BootstrapBinder";

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Vertx vertx, Container container, ClassLoader cl) {
        super.init(vertx, container, cl);

        this.vertx = vertx;
        this.container = container;
        this.cl = cl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Verticle createVerticle(String main) throws Exception {
        String className = main;
        Class<?> clazz;

        if (isJavaSource(main)) {
            // TODO - is this right???
            // Don't we want one CompilingClassLoader per instance of this?
            CompilingClassLoader compilingLoader = new CompilingClassLoader(cl, main);
            className = compilingLoader.resolveMainClassName();
            clazz = compilingLoader.loadClass(className);
        } else {
            clazz = cl.loadClass(className);
        }
        Verticle verticle = createVerticle(clazz);
        verticle.setVertx(vertx);
        verticle.setContainer(container);
        return verticle;
    }

    private Verticle createVerticle(Class<?> clazz) throws Exception {

        String bootstrapName = BOOTSTRAP_BINDER_NAME;
        Module bootstrap = null;

        try {
            Class bootstrapClass = cl.loadClass(bootstrapName);
            Object obj = bootstrapClass.newInstance();

            if (obj instanceof Module) {
                bootstrap = (Module) obj;
            } else {
                logger.error("Class " + bootstrapName
                        + " does not implement Binder.");
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Guice bootstrap binder class " + bootstrapName
                    + " was not found.  Are you missing injection bindings?");
        }

        List<Module> modules = new ArrayList<>();
        modules.add(new VertxBinder(vertx, container));

        // Add bootstrap if it exists
        if (bootstrap != null) {
            modules.add(bootstrap);
        }

        // Each verticle factory will have it's own injector instance
        Injector injector = Guice.createInjector(modules);
        return (Verticle) injector.getInstance(clazz);
    }

    private boolean isJavaSource(String main) {
        return main.endsWith(".java");
    }

}
