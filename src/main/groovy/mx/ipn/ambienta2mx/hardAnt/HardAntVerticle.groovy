package mx.ipn.ambienta2mx.hardAnt

import mx.ipn.ambienta2mx.hardAnt.services.DataTypeService
import mx.ipn.ambienta2mx.hardAnt.verticles.DataTypeVerticle
import org.vertx.groovy.platform.Verticle

class HardAntVerticle extends Verticle {
    Map definedConfiguration

    def start() {
        definedConfiguration = container.getConfig();
        container.logger.info("Hard Ant verticle started");
        // MX 1 database
        container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', definedConfiguration.mongo1)
        // MX 2 database
        container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', definedConfiguration.mongo2)
        // MX 3 database
        container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', definedConfiguration.mongo3)
        // MX 4 database
        container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', definedConfiguration.mongo4)
        // DataType Finder Verticle
        container.deployVerticle("groovy:" + DataTypeService.class.getCanonicalName(), definedConfiguration)
        // DataType Verticle
        container.deployVerticle("groovy:" + DataTypeVerticle.class.getCanonicalName(), definedConfiguration)
    }

    def stop() {
        container.logger.info("Hard Ant main verticle has stopped!");
    }
}
