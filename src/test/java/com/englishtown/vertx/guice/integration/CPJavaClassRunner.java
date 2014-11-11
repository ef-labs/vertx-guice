package com.englishtown.vertx.guice.integration;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


/**
* User: adriangonzalez
*/
public class CPJavaClassRunner extends BlockJUnit4ClassRunner {

    private static final Logger log = LoggerFactory.getLogger(CPJavaClassRunner.class);

    protected static final long TIMEOUT;
    private static final long DEFAULT_TIMEOUT = 300;
    static {
        String timeout = System.getProperty("vertx.test.timeout");
        TIMEOUT = timeout == null ? DEFAULT_TIMEOUT : Long.valueOf(timeout);
    }

    public static final String TESTRUNNER_HANDLER_ADDRESS = "vertx.testframework.handler";

    private final Vertx vertx;
    protected String main;

    public CPJavaClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
        vertx = Vertx.vertx();
    }

    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Class<?> testClass = getTestClass().getJavaClass();
        String methodName = method.getName();
        String testDesc = method.getName();
        Description desc = Description.createTestDescription(testClass, testDesc);
        notifier.fireTestStarted(desc);
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        try {
            JsonObject conf = new JsonObject().put("methodName", methodName);
            final CountDownLatch testLatch = new CountDownLatch(1);
            Handler<Message<JsonObject>> handler = (Message<JsonObject> msg) -> {
                        JsonObject jmsg = msg.body();
                        String type = jmsg.getString("type");
                        try {
                            switch (type) {
                                case "done":
                                    break;
                                case "failure":
                                    byte[] bytes = jmsg.getBinary("failure");
                                    // Deserialize
                                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                                    Throwable t = (Throwable) ois.readObject();
                                    // We display this since otherwise Gradle doesn't display it to stdout/stderr
                                    t.printStackTrace();
                                    failure.set(t);
                                    break;
                            }
                        } catch (ClassNotFoundException | IOException e) {
                            e.printStackTrace();
                            failure.set(e);
                        } finally {
                            testLatch.countDown();
                        }
            };


            EventBus eb = Vertx.vertx().eventBus();
            MessageConsumer consumer = eb.consumer(TESTRUNNER_HANDLER_ADDRESS);
            consumer.handler(handler);
            final CountDownLatch deployLatch = new CountDownLatch(1);
            final AtomicReference<String> deploymentIDRef = new AtomicReference<>();
            System.out.println("Starting test: " + testDesc);
            String main = getMain(methodName);
            URL[] urls = getClassPaths(methodName);
            final AtomicReference<Throwable> deployThrowable = new AtomicReference<>();
            DeploymentOptions options = new DeploymentOptions(conf);
            options.setInstances(1);
            //TODO Migration: Set additional params? urls, 1, includes
            Vertx.vertx().deployVerticle(main, options, ar -> {
                if (ar.succeeded()) {
                    deploymentIDRef.set(ar.result());
                } else {
                    deployThrowable.set(ar.cause());
                }
                deployLatch.countDown();
            });
            waitForLatch(deployLatch);
            if (deployThrowable.get() != null) {
                notifier.fireTestFailure(new Failure(desc, deployThrowable.get()));
                notifier.fireTestFinished(desc);
                return;
            }
            waitForLatch(testLatch);
            consumer.unregister();
            final CountDownLatch undeployLatch = new CountDownLatch(1);
            final AtomicReference<Throwable> undeployThrowable = new AtomicReference<>();
            vertx.undeployVerticle(deploymentIDRef.get(), ar -> {
                if (ar.failed()) {
                    undeployThrowable.set(ar.cause());
                }
                undeployLatch.countDown();
            });
            waitForLatch(undeployLatch);
            if (undeployThrowable.get() != null) {
                notifier.fireTestFailure(new Failure(desc, undeployThrowable.get()));
                notifier.fireTestFinished(desc);
                return;
            }
            if (failure.get() != null) {
                notifier.fireTestFailure(new Failure(desc, failure.get()));
            }
            notifier.fireTestFinished(desc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected URL[] getClassPaths(String methodName) {
        List<URL> urls = new ArrayList<>();

        String classPaths = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        String fileSeparator = System.getProperty("file.separator");

        // Include everything on the classpath except for java jars and vertx-core/vertx-platform
        String[] cps = classPaths.split(pathSeparator);
        String javaHome = System.getProperty("java.home");
        String vertxCore = fileSeparator + "vertx-core" + fileSeparator;

        for (String s : cps) {
            if (!s.startsWith(javaHome) && !s.contains(vertxCore)) {
                File f = new File(s);
                if (f.exists()) {
                    try {
                        urls.add(f.toURI().toURL());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return urls.toArray(new URL[urls.size()]);
    }

    private void waitForLatch(CountDownLatch latch) {
        while (true) {
            try {
                if (!latch.await(TIMEOUT, TimeUnit.SECONDS)) {
                    throw new AssertionError("Timed out waiting for test to complete");
                }
                break;
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    protected String getMain(String methodName) {
        return main;
    }
}
