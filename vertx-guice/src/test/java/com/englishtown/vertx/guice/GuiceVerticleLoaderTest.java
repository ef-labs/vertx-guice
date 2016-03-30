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

import com.englishtown.vertx.guice.integration.CustomBinder;
import com.englishtown.vertx.guice.integration.DependencyInjectionVerticle;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.logging.LogDelegate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GuiceVerticleLoader}
 */
@RunWith(MockitoJUnitRunner.class)
public class GuiceVerticleLoaderTest {

    private JsonObject config = new JsonObject();
    private static LogDelegate logger;

    @Mock
    private Vertx vertx;
    @Mock
    private Context context;
    @Mock
    private Injector parent;
    @Mock
    private Injector child;
    @Mock
    private Verticle verticle;
    @Mock
    private Future<Void> future;
    @Captor
    private ArgumentCaptor<Iterable<Module>> modulesCaptor;
    @Captor
    private ArgumentCaptor<Class<Verticle>> classCaptor;

    @BeforeClass
    public static void setUpOnce() {
        logger = MockLogDelegateFactory.getLogDelegate();
    }

    @Before
    public void setUp() {
        MockLogDelegateFactory.reset();
        when(context.config()).thenReturn(config);
        when(parent.createChildInjector(Mockito.<Iterable<? extends Module>>any())).thenReturn(child);
        when(child.getInstance(any(Class.class))).thenReturn(verticle);
    }

    private GuiceVerticleLoader doTest(String main) throws Exception {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        GuiceVerticleLoader loader = new GuiceVerticleLoader(main, cl, parent);
        loader.init(vertx, context);
        loader.start(future);

        verify(verticle).init(eq(vertx), eq(context));
        verify(verticle).start(future);
        verify(parent).createChildInjector(modulesCaptor.capture());
        verify(child).getInstance(classCaptor.capture());

        loader.stop(future);
        verify(verticle).stop(eq(future));

        return loader;
    }

    private List<Module> getModules() {
        return Lists.newArrayList(modulesCaptor.getValue());
    }

    @Test
    public void testStart_Compiled() throws Exception {

        String main = DependencyInjectionVerticle.class.getName();
        doTest(main);

        verifyZeroInteractions(logger);
        assertEquals(DependencyInjectionVerticle.class, classCaptor.getValue());

    }

    @Test
    public void testStart_Uncompiled() throws Exception {

        String main = "UncompiledDIVerticle.java";
        doTest(main);

        verifyZeroInteractions(logger);
        assertEquals("UncompiledDIVerticle", classCaptor.getValue().getName());

    }

    @Test
    public void testStart_Custom_Binder() throws Exception {

        config.put("guice_binder", CustomBinder.class.getName());

        String main = DependencyInjectionVerticle.class.getName();
        doTest(main);

        verifyZeroInteractions(logger);
        assertEquals(DependencyInjectionVerticle.class, classCaptor.getValue());

        List<Module> modules = getModules();
        assertEquals(2, modules.size());
        assertEquals(CustomBinder.class, modules.get(0).getClass());

    }

    @Test
    public void testStart_Not_A_Binder() throws Exception {

        String binder = String.class.getName();
        config.put("guice_binder", binder);

        String main = DependencyInjectionVerticle.class.getName();
        doTest(main);

        verify(logger).error(eq("Class " + binder + " does not implement Module."));
        assertEquals(1, getModules().size());

    }

    @Test
    public void testStart_Class_Not_Found_Binder() throws Exception {

        String binder = "com.englishtown.INVALID_BINDER";
        config.put("guice_binder", binder);

        String main = DependencyInjectionVerticle.class.getName();
        doTest(main);

        verify(logger).error(eq("Guice bootstrap binder class " + binder + " was not found.  Are you missing injection bindings?"));
        assertEquals(1, getModules().size());

    }

}
