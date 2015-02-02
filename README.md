[![Build Status](https://travis-ci.org/englishtown/vertx-guice.png)](https://travis-ci.org/englishtown/vertx-guice)

# Vert.x Guice Extensions
Enable Verticle dependency injection using Guice.  Deploy your verticle with the `java-guice:` prefix to use the `GuiceVerticleFactory`.


## License
http://englishtown.mit-license.org/


## Configuration

Either provide a com.englishtown.vertx.guice.BootstrapBinder that implements com.google.inject.Module, or via vert.x config, provide a custom class name.

```json
{
    "guice_binder": "my.custom.bootstrap.Binder"
}
```

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
