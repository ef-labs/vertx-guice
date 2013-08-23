[![Build Status](https://travis-ci.org/englishtown/vertx-mod-guice.png)](https://travis-ci.org/englishtown/vertx-mod-guice)

# Vert.x Guice Module
Enable Verticle and Module dependency injection using Guice.  The default Vert.x Java VerticleFactory is replaced with
com.englishtown.vertx.guice.GuiceVerticleFactory for Verticle construction.


## License
http://englishtown.mit-license.org/


## Configuration

To configure Vert.x to use the GuiceVerticleFactory modify the langs.properties java entry like this:
`java=com.englishtown~vertx-mod-guice~1.0.0-final:com.englishtown.vertx.guice.GuiceVerticleFactory`

or set a system property when running:
`-Dvertx.langs.java=com.englishtown~vertx-mod-guice~1.0.0-final:com.englishtown.vertx.guice.GuiceVerticleFactory`


## Example

```java
package com.englishtown.vertx.guice;

import com.englishtown.configuration.ConfigValueManager;
import com.englishtown.configuration.OtherBinder1;
import com.englishtown.configuration.OtherBinder2;
import com.englishtown.configuration.impl.PropertiesConfigValueManager;
import com.google.inject.AbstractModule;

import javax.inject.Singleton;

public class BootstrapBinder extends AbstractModule {

    @Override
    protected void configure() {

        // Configure bindings
        bind(ConfigValueManager.class).to(PropertiesConfigValueManager.class).in(Singleton.class);

        // Install other binders
        install(new OtherBinder1(), new OtherBinder2());

    }

}
```
