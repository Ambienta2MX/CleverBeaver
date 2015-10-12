package mx.ipn.ambienta2mx.hardAnt

import mx.ipn.ambienta2mx.hardAnt.web.LocationVerticle
import mx.ipn.ambienta2mx.hardAnt.web.QueryVerticle
import org.vertx.groovy.platform.Verticle

class HardAntVerticle extends Verticle {
    Map definedConfiguration

    def start() {
        container.logger.info("Hard Ant verticle started");

    }

    def stop() {
        container.logger.info("Hard Ant main verticle has stopped!");
    }
}
