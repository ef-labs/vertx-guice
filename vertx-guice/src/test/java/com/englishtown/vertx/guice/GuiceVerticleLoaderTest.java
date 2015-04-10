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
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.impl.LogDelegate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GuiceVerticleLoader}
 */
@RunWith(MockitoJUnitRunner.class)
public class GuiceVerticleLoaderTest {

    private JsonObject config = new JsonObject();
    private static LogDelegate logger;

    @Mock
    Vertx vertx;
    @Mock
    Context context;
    @Mock
    Future<Void> future;

    @BeforeClass
    public static void setUpOnce() {
        logger = MockLogDelegateFactory.getLogDelegate();
    }

    @Before
    public void setUp() {
        MockLogDelegateFactory.reset();
        when(vertx.getOrCreateContext()).thenReturn(context);
        when(context.config()).thenReturn(config);
    }

    private GuiceVerticleLoader createLoader(String main) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        GuiceVerticleLoader loader = new GuiceVerticleLoader(main, cl);
        loader.init(vertx, vertx.getOrCreateContext());
        return loader;
    }

    @Test
    public void testStart_Compiled() throws Exception {

        String main = DependencyInjectionVerticle.class.getName();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(future);

        verify(future).complete();
        verify(future, never()).fail(Mockito.<Throwable>any());
        verifyZeroInteractions(logger);
        loader.stop();

    }

    @Test
    public void testStart_Uncompiled() throws Exception {

        String main = "UncompiledDIVerticle.java";

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(future);

        verify(future).complete();
        verify(future, never()).fail(Mockito.<Throwable>any());
        verifyZeroInteractions(logger);
        loader.stop();

    }

    @Test
    public void testStart_Custom_Binder() throws Exception {

        config.put("guice_binder", CustomBinder.class.getName());

        String main = DependencyInjectionVerticle.class.getName();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(future);

        verify(future).complete();
        verify(future, never()).fail(Mockito.<Throwable>any());
        verifyZeroInteractions(logger);
        loader.stop();

    }

    @Test
    public void testStart_Not_A_Binder() throws Exception {

        String binder = String.class.getName();
        config.put("guice_binder", binder);

        String main = DependencyInjectionVerticle.class.getName();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(future);

        verify(future, never()).complete();
        verify(future).fail(Mockito.<Throwable>any());
        verify(logger).error(eq("Class " + binder + " does not implement Module."));
        loader.stop();

    }

    @Test
    public void testStart_Class_Not_Found_Binder() throws Exception {

        String binder = "com.englishtown.INVALID_BINDER";
        config.put("guice_binder", binder);

        String main = DependencyInjectionVerticle.class.getName();

        GuiceVerticleLoader loader = createLoader(main);
        loader.start(future);

        verify(future, never()).complete();
        verify(future).fail(Mockito.<Throwable>any());
        verify(logger).error(eq("Guice bootstrap binder class " + binder + " was not found.  Are you missing injection bindings?"));
        loader.stop();

    }

}
