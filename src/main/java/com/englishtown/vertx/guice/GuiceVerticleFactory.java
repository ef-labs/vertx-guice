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

import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.VerticleFactory;
import org.vertx.java.platform.impl.java.JavaVerticleFactory;

/**
 * Extends the default vert.x {@link JavaVerticleFactory} using Guice for dependency injection.
 */
public class GuiceVerticleFactory implements VerticleFactory {

    private Vertx vertx;
    private Container container;
    private ClassLoader cl;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Vertx vertx, Container container, ClassLoader cl) {
        this.vertx = vertx;
        this.container = container;
        this.cl = cl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Verticle createVerticle(String main) throws Exception {
        Verticle verticle = new GuiceVerticleLoader(main, cl);
        verticle.setVertx(vertx);
        verticle.setContainer(container);
        return verticle;
    }

    @Override
    public void reportException(Logger logger, Throwable t) {
        if (logger != null) {
            logger.error("Exception in GuiceVerticleFactory", t);
        }
    }

    @Override
    public void close() {
    }

}
