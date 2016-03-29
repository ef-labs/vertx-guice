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

import com.google.inject.Injector;
import io.vertx.core.Context;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GuiceVerticleFactory}
 */
@RunWith(MockitoJUnitRunner.class)
public class GuiceVerticleFactoryTest {

    private GuiceVerticleFactory factory;
    private JsonObject config = new JsonObject();

    @Mock
    private Vertx vertx;
    @Mock
    private Context context;

    @Before
    public void setUp() throws Exception {
        when(vertx.getOrCreateContext()).thenReturn(context);
        when(context.config()).thenReturn(config);

        factory = new GuiceVerticleFactory();
        factory.init(vertx);
    }

    @Test
    public void testPrefix() {
        assertEquals("java-guice", factory.prefix());
    }

    @Test
    public void testCreateVerticle() throws Exception {
        String identifier = GuiceVerticleFactory.PREFIX + ":" + TestGuiceVerticle.class.getName();
        Verticle verticle = factory.createVerticle(identifier, this.getClass().getClassLoader());
        assertThat(verticle, instanceOf(GuiceVerticleLoader.class));

        GuiceVerticleLoader loader = (GuiceVerticleLoader) verticle;
        assertEquals(TestGuiceVerticle.class.getName(), loader.getVerticleName());
    }

    @Test
    public void testSetInjector() throws Exception {

        Injector original = factory.getInjector();
        assertNotNull(original);

        Injector injector = mock(Injector.class);
        factory.setInjector(injector);

        assertEquals(injector, factory.getInjector());

    }

}
