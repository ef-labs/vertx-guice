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

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.impl.JavaVerticleFactory;
import io.vertx.core.spi.VerticleFactory;

/**
 * Extends the default vert.x {@link JavaVerticleFactory} using Guice for dependency injection.
 */
public class GuiceVerticleFactory implements VerticleFactory {

    private Vertx vertx;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Verticle createVerticle(String main, ClassLoader cl) throws Exception {
        Verticle verticle = new GuiceVerticleLoader(main, cl);
        //TODO Migration: Do we need to init the verticle and set vertx?
//        verticle.setVertx(vertx);
        return verticle;
    }

    @Override
    public void close() {
    }

    @Override
    public String prefix() {
        // TODO Migration: Check whether a prefix is needed
        return null;
    }

}
