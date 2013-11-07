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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GuiceVerticleFactory}
 */
@RunWith(MockitoJUnitRunner.class)
public class GuiceVerticleFactoryTest {

    GuiceVerticleFactory factory = new GuiceVerticleFactory();
    JsonObject config = new JsonObject();

    @Mock
    Vertx vertx;
    @Mock
    Container container;
    @Mock
    Logger logger;

    @Before
    public void setUp() {
        when(container.config()).thenReturn(config);
        when(container.logger()).thenReturn(logger);
    }

    @Test
    public void testCreateVerticle() throws Exception {

        config.putString("guice_binder", "com.englishtown.vertx.guice.BootstrapBinder");

        factory.init(vertx, container, this.getClass().getClassLoader());
        factory.createVerticle("com.englishtown.vertx.guice.TestGuiceVerticle");

    }

    @Test
    public void testReportException() throws Exception {

        factory.reportException(null, null);
        factory.reportException(logger, new RuntimeException());

    }

    @Test
    public void testClose() throws Exception {

        factory.close();

    }

}
