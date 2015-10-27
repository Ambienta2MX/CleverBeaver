package mx.ipn.ambienta2mx.hardAnt.verticles

import mx.ipn.ambienta2mx.hardAnt.verticles.routes.PollutionRouter
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle

/**
 * Created by alberto on 16/10/15.
 */
class PollutionVerticle extends Verticle{
    Map definedConfiguration
    def server
    RouteMatcher routeMatcher
    PollutionRouter pollutionRouter

    def start() {
        container.logger.info("Weather Verticle has deployed");
        //
        definedConfiguration = container.getConfig()
        server = vertx.createHttpServer()
        routeMatcher = new RouteMatcher()
        //
        pollutionRouter =  new PollutionRouter()
        pollutionRouter.definedConfiguration = this.definedConfiguration
        pollutionRouter.eventBus = vertx.eventBus
        pollutionRouter.container = this.container
        //
        routeMatcher.post("/pollution/save/",pollutionRouter.savePollutionByLatLon)
        routeMatcher.get("/pollution/find/:lattitude/:longitude/:max", pollutionRouter.findPollutionByLatLon)
        routeMatcher.get("/pollution/find/", pollutionRouter.findPollutionByPlaceName)
        //
        server.requestHandler(routeMatcher.asClosure()).listen(definedConfiguration.pollutionVerticle.http.port, definedConfiguration.pollutionVerticle.http.host);
    }
}
