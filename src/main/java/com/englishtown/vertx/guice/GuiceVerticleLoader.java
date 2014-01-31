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
import org.vertx.java.core.Future;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.impl.java.CompilingClassLoader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Guice Verticle to lazy load the real verticle with DI
 */
public class GuiceVerticleLoader extends Verticle {

    private final String main;
    private final ClassLoader cl;
    private Verticle realVerticle;

    public static final String CONFIG_BOOTSTRAP_BINDER_NAME = "guice_binder";
    public static final String BOOTSTRAP_BINDER_NAME = "com.englishtown.vertx.guice.BootstrapBinder";

    public GuiceVerticleLoader(String main, ClassLoader cl) {
        this.main = main;
        this.cl = cl;
    }

    /**
     * Override this method to signify that start is complete sometime _after_ the start() method has returned
     * This is useful if your verticle deploys other verticles or modules and you don't want this verticle to
     * be considered started until the other modules and verticles have been started.
     *
     * @param startedResult When you are happy your verticle is started set the result
     */
    @Override
    public void start(Future<Void> startedResult) {

        // Create the real verticle
        try {
            realVerticle = createRealVerticle();
        } catch (Exception e) {
            startedResult.setFailure(e);
            return;
        }

        // Start the real verticle
        realVerticle.start(startedResult);

    }

    /**
     * Vert.x calls the stop method when the verticle is undeployed.
     * Put any cleanup code for your verticle in here
     */
    @Override
    public void stop() {
        // Stop the real verticle
        if (realVerticle != null) {
            realVerticle.stop();
            realVerticle = null;
        }
    }

    public Verticle createRealVerticle() throws Exception {
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
        Verticle verticle = createRealVerticle(clazz);
        verticle.setVertx(vertx);
        verticle.setContainer(container);
        return verticle;
    }

    private Verticle createRealVerticle(Class<?> clazz) throws Exception {

        JsonObject config = container.config();
        String bootstrapName = config.getString(CONFIG_BOOTSTRAP_BINDER_NAME, BOOTSTRAP_BINDER_NAME);
        Module bootstrap = null;

        try {
            Class<?> bootstrapClass = cl.loadClass(bootstrapName);
            Object obj;
            try {
                Constructor<?> constructor = bootstrapClass.getConstructor(Vertx.class, Container.class);
                obj = constructor.newInstance(vertx, container);
            } catch (NoSuchMethodException | SecurityException ignored){
                obj = bootstrapClass.newInstance();
            }


            if (obj instanceof Module) {
                bootstrap = (Module) obj;
            } else {
                container.logger().error("Class " + bootstrapName
                        + " does not implement Module.");
            }
        } catch (ClassNotFoundException e) {
            container.logger().error("Guice bootstrap binder class " + bootstrapName
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
