package mx.ipn.ambienta2mx.hardAnt.verticles

import mx.ipn.ambienta2mx.hardAnt.verticles.routes.DataTypeRouter
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle

/**
 * Created by alberto on 16/10/15.
 */
class DataTypeVerticle extends Verticle {
    Map definedConfiguration
    def server
    RouteMatcher routeMatcher
    DataTypeRouter dataTypeRouter

    def start() {
        container.logger.info("DataType Verticle has deployed");
        //
        definedConfiguration = container.getConfig()
        server = vertx.createHttpServer()
        routeMatcher = new RouteMatcher()
        //
        dataTypeRouter =  new DataTypeRouter()
        dataTypeRouter.definedConfiguration = this.definedConfiguration
        dataTypeRouter.eventBus = vertx.eventBus
        dataTypeRouter.container = this.container
        //
        routeMatcher.post("/:dataType",dataTypeRouter.saveModel)
        routeMatcher.get("/:dataType", dataTypeRouter.findDataTypeBy)
        routeMatcher.options("/:dataType", dataTypeRouter.findDataTypeBy)

        //
        server.requestHandler(routeMatcher.asClosure()).listen(definedConfiguration.DataTypeVerticle.http.port, definedConfiguration.DataTypeVerticle.http.host);
    }
}
