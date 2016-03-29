/*
 * The MIT License (MIT)
 * Copyright © 2016 Englishtown <opensource@englishtown.com>
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

import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.*;
import io.vertx.core.impl.verticle.CompilingClassLoader;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Guice Verticle to lazy load the real verticle with DI
 */
public class GuiceVerticleLoader extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(GuiceVerticleLoader.class);

    private final String verticleName;
    private final ClassLoader classLoader;
    private final Injector parent;
    private Verticle realVerticle;

    public static final String CONFIG_BOOTSTRAP_BINDER_NAME = "guice_binder";
    public static final String BOOTSTRAP_BINDER_NAME = "com.englishtown.vertx.guice.BootstrapBinder";

    public GuiceVerticleLoader(String verticleName, ClassLoader classLoader, Injector parent) {
        this.verticleName = verticleName;
        this.classLoader = classLoader;
        this.parent = parent;
    }

    /**
     * Initialise the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.
     *
     * @param vertx   the deploying Vert.x instance
     * @param context the context of the verticle
     */
    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        try {
            // Create the real verticle and init
            realVerticle = createRealVerticle();
            realVerticle.init(vertx, context);

        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }

    /**
     * Override this method to signify that start is complete sometime _after_ the start() method has returned
     * This is useful if your verticle deploys other verticles or modules and you don't want this verticle to
     * be considered started until the other modules and verticles have been started.
     *
     * @param startedResult When you are happy your verticle is started set the result
     * @throws Exception
     */
    @Override
    public void start(Future<Void> startedResult) throws Exception {
        // Start the real verticle
        realVerticle.start(startedResult);
    }

    /**
     * Vert.x calls the stop method when the verticle is undeployed.
     * Put any cleanup code for your verticle in here
     *
     * @throws Exception
     */
    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        // Stop the real verticle
        if (realVerticle != null) {
            realVerticle.stop(stopFuture);
            realVerticle = null;
        }
    }

    public String getVerticleName() {
        return verticleName;
    }

    public Verticle createRealVerticle() throws Exception {
        String className = verticleName;
        Class<?> clazz;

        if (className.endsWith(".java")) {
            CompilingClassLoader compilingLoader = new CompilingClassLoader(classLoader, className);
            className = compilingLoader.resolveMainClassName();
            clazz = compilingLoader.loadClass(className);
        } else {
            clazz = classLoader.loadClass(className);
        }
        Verticle verticle = createRealVerticle(clazz);
        return verticle;
    }

    private Verticle createRealVerticle(Class<?> clazz) throws Exception {

        JsonObject config = config();
        Object field = config.getValue(CONFIG_BOOTSTRAP_BINDER_NAME);
        JsonArray bootstrapNames;
        List<Module> bootstraps = new ArrayList<>();

        if (field instanceof JsonArray) {
            bootstrapNames = (JsonArray) field;
        } else {
            bootstrapNames = new JsonArray().add((field == null ? BOOTSTRAP_BINDER_NAME : field));
        }

        for (int i = 0; i < bootstrapNames.size(); i++) {
            String bootstrapName = bootstrapNames.getString(i);
            try {
                Class bootstrapClass = classLoader.loadClass(bootstrapName);
                Object obj = bootstrapClass.newInstance();

                if (obj instanceof Module) {
                    bootstraps.add((Module) obj);
                } else {
                    logger.error("Class " + bootstrapName
                            + " does not implement Module.");
                }
            } catch (ClassNotFoundException e) {
                logger.error("Guice bootstrap binder class " + bootstrapName
                        + " was not found.  Are you missing injection bindings?");
            }
        }

        // Each verticle factory will have it's own child injector instance
        Injector injector = parent.createChildInjector(bootstraps);
        return (Verticle) injector.getInstance(clazz);
    }

}
