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

import com.englishtown.vertx.guice.integration.CustomBinder;
import com.englishtown.vertx.guice.integration.DependencyInjectionVerticle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GuiceVerticleLoader}
 */
@RunWith(MockitoJUnitRunner.class)
public class GuiceVerticleLoaderTest {

    JsonObject config = new JsonObject();

    @Mock
    Vertx vertx;
    @Mock
    Container container;
    @Mock
    Logger logger;

    @Before
    public void setUp() {

        when(container.logger()).thenReturn(logger);
        when(container.config()).thenReturn(config);

    }

    private GuiceVerticleLoader createLoader(String main) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        GuiceVerticleLoader loader = new GuiceVerticleLoader(main, cl);
        loader.setContainer(container);
        loader.setVertx(vertx);

        return loader;
    }

    @Test
    public void testStart_Compiled() throws Exception {

        String main = DependencyInjectionVerticle.class.getName();
        DefaultFutureResult<Void> vr = new DefaultFutureResult<>();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertTrue(vr.succeeded());
        verifyZeroInteractions(logger);
        loader.stop();

    }

    @Test
    public void testStart_Uncompiled() throws Exception {

        String main = "UncompiledDIVerticle.java";
        DefaultFutureResult<Void> vr = new DefaultFutureResult<>();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertTrue(vr.succeeded());
        verifyZeroInteractions(logger);
        loader.stop();

    }

    @Test
    public void testStart_Custom_Binder() throws Exception {

        config.putString("guice_binder", CustomBinder.class.getName());

        String main = DependencyInjectionVerticle.class.getName();
        DefaultFutureResult<Void> vr = new DefaultFutureResult<>();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertTrue(vr.succeeded());
        verifyZeroInteractions(logger);
        loader.stop();

    }

    @Test
    public void testStart_Not_A_Binder() throws Exception {

        String binder = String.class.getName();
        config.putString("guice_binder", binder);

        String main = DependencyInjectionVerticle.class.getName();
        DefaultFutureResult<Void> vr = new DefaultFutureResult<>();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertFalse(vr.succeeded());
        assertNotNull(vr.cause());
        verify(logger).error(eq("Class " + binder + " does not implement Module."));
        loader.stop();

    }

    @Test
    public void testStart_Class_Not_Found_Binder() throws Exception {

        String binder = "com.englishtown.INVALID_BINDER";
        config.putString("guice_binder", binder);

        String main = DependencyInjectionVerticle.class.getName();
        DefaultFutureResult<Void> vr = new DefaultFutureResult<>();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertFalse(vr.succeeded());
        assertNotNull(vr.cause());
        verify(logger).error(eq("Guice bootstrap binder class " + binder
                + " was not found.  Are you missing injection bindings?"));
        loader.stop();

    }

}
