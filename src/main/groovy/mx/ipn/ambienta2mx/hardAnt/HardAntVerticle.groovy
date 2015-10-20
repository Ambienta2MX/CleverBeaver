package mx.ipn.ambienta2mx.hardAnt

import mx.ipn.ambienta2mx.hardAnt.verticles.PollutionVerticle
import mx.ipn.ambienta2mx.hardAnt.verticles.WeatherVerticle
import org.vertx.groovy.platform.Verticle

class HardAntVerticle extends Verticle {
    Map definedConfiguration

    def start() {
        definedConfiguration = container.getConfig();
        container.logger.info("Hard Ant verticle started");
        // MX 1 databas
        //container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', definedConfiguration.mongo1)
        // MX 2 databas
        //container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', definedConfiguration.mongo2)
        // MX 3 databas
        //container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', definedConfiguration.mongo3)
        // MX 4 databas
        //container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', definedConfiguration.mongo4)
        // Pollution verticle service
        //container.deployVerticle("groovy:" + PollutionVerticle.class.getCanonicalName(), definedConfiguration)
        // Google Maps Solver verticle
        //container.deployVerticle("groovy:" + WeatherVerticle.class.getCanonicalName(), definedConfiguration)
        /*
        * Testing
        * */
        vertx.eventBus.send("Ambienta2MX.FastEagle.EventBus.Location")
     }

    def stop() {
        container.logger.info("Hard Ant main verticle has stopped!");
    }
}
