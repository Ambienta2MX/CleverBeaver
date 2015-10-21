package mx.ipn.ambienta2mx.hardAnt.verticles.routes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Created by alberto on 16/10/15.
 */
class WeatherRouter {
    def definedConfiguration
    def container
    def eventBus

    def saveWeatherByLatLon = { request ->
        request.bodyHandler { body ->
            def weatherMap = new JsonSlurper().parseText("$body")
            def fastEagleService = definedConfiguration.fastEagleService
            def url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.url
            url = url.replace(":latitude", "$weatherMap.latitude")
            url = url.replace(":longitude", "$weatherMap.longitude")
            url = url.replace(":maxDistance", "${request.params.maxDistance ?: 100}")
            def urlObject = new URL(url)
            def place = new JsonSlurper().parse(urlObject)
            if (place) {
                def mongoOperation = [action: 'save', collection: 'Weather']
                mongoOperation.document = weatherMap;
                def database = definedConfiguration.states[place[0].state];
                eventBus.send("${definedConfiguration.databasesAddress}.${database}", mongoOperation) { result ->
                    println "Se guardó! o hizo algo en la base XP ";
                    if (result) {
                        request.response.end("${JsonOutput.toJson(weatherMap)}")
                    } else {
                        request.response.code = 404;
                        request.response.end("{'save':false}")
                    }

                }
            }
        }
    } as groovy.lang.Closure

    def findWeatherByLatLon = { request ->
        request.response.end("${JsonOutput.toJson(request.query)}");
    } as groovy.lang.Closure
}
