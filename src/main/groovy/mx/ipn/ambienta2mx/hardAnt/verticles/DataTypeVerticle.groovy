package mx.ipn.ambienta2mx.hardAnt.verticles

import mx.ipn.ambienta2mx.hardAnt.verticles.routes.WeatherRouter
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle

/**
 * Created by alberto on 16/10/15.
 */
class DataTypeVerticle extends Verticle {
    Map definedConfiguration
    def server
    RouteMatcher routeMatcher
    WeatherRouter weatherRouter

    def start() {
        container.logger.info("Weather Verticle has deployed");
        //
        definedConfiguration = container.getConfig()
        server = vertx.createHttpServer()
        routeMatcher = new RouteMatcher()
        //
        weatherRouter =  new WeatherRouter()
        weatherRouter.definedConfiguration = this.definedConfiguration
        weatherRouter.eventBus = vertx.eventBus
        weatherRouter.container = this.container
        //
        routeMatcher.post("/weather",weatherRouter.saveWeather)
        routeMatcher.get("/weather", weatherRouter.findWeatherBy)
        routeMatcher.options("/weather", weatherRouter.findWeatherBy)

        //
        server.requestHandler(routeMatcher.asClosure()).listen(definedConfiguration.weatherVerticle.http.port, definedConfiguration.weatherVerticle.http.host);
    }
}