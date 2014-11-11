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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.impl.LoggerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.englishtown.vertx.guice.integration.CustomBinder;
import com.englishtown.vertx.guice.integration.DependencyInjectionVerticle;

/**
 * Unit tests for {@link GuiceVerticleLoader}
 */
@RunWith(MockitoJUnitRunner.class)
public class GuiceVerticleLoaderTest {

    JsonObject config = new JsonObject();

    @Mock
    Vertx vertx;
    @Mock
    Context context;

    @Before
    public void setUp() {
        // Use our own test logger factory / logger instead. We can't use powermock to statically mock the
        // LoggerFactory since javassist 1.18.x contains a bug that prevents the usage of powermock.
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, LogDelegateTestFactory.class.getCanonicalName());
        LoggerFactory.initialise();
        TestLogDelegate.reset();
        when(vertx.context()).thenReturn(context);
        when(context.config()).thenReturn(config);
    }

    private GuiceVerticleLoader createLoader(String main) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        GuiceVerticleLoader loader = new GuiceVerticleLoader(main, cl);
        loader.init(vertx, vertx.context());
        return loader;
    }

    @Test
    public void testStart_Compiled() throws Exception {

        String main = DependencyInjectionVerticle.class.getName();
        Future<Void> vr = Future.future();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertTrue(vr.succeeded());
        assertFalse("The logger should not have been used.", TestLogDelegate.wasUsed());
        loader.stop();

    }

    @Test
    public void testStart_Uncompiled() throws Exception {

        String main = "UncompiledDIVerticle.java";
        Future<Void> vr = Future.future();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertTrue("The verticle creation should have been successful.", vr.succeeded());
        assertFalse("The logger should not have been used.", TestLogDelegate.wasUsed());
        loader.stop();

    }

    @Test
    public void testStart_Custom_Binder() throws Exception {

        config.put("guice_binder", CustomBinder.class.getName());

        String main = DependencyInjectionVerticle.class.getName();
        Future<Void> vr = Future.future();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertTrue("The verticle creation should have been successful.", vr.succeeded());
        assertFalse("The logger should not have been used.", TestLogDelegate.wasUsed());
        loader.stop();

    }

    @Test
    public void testStart_Not_A_Binder() throws Exception {

        String binder = String.class.getName();
        config.put("guice_binder", binder);

        String main = DependencyInjectionVerticle.class.getName();
        Future<Void> vr = Future.future();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertFalse("The verticle creation should fail.", vr.succeeded());
        assertNotNull(vr.cause());
        assertEquals("Class " + binder + " does not implement Module.", TestLogDelegate.getLastError());

        loader.stop();

    }

    @Test
    public void testStart_Class_Not_Found_Binder() throws Exception {

        String binder = "com.englishtown.INVALID_BINDER";
        config.put("guice_binder", binder);

        String main = DependencyInjectionVerticle.class.getName();
        Future<Void> vr = Future.future();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(vr);

        assertFalse("The verticle creation should fail.", vr.succeeded());
        assertNotNull(vr.cause());
        assertEquals("Guice bootstrap binder class " + binder
                + " was not found.  Are you missing injection bindings?", TestLogDelegate.getLastError());
        loader.stop();

    }

}
