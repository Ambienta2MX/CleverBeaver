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
            String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.longitudeLatitudeService
            url = url.replace(":latitude", "$weatherMap.latitude")
            url = url.replace(":longitude", "$weatherMap.longitude")
            url = url.replace(":maxDistance", "${request.params.maxDistance ?: 100}")
            def urlObject = new URL(url)
            def place = new JsonSlurper().parse(urlObject)

            request.response.putHeader("Content-Type", "application/json")

            if (place) {
                def mongoOperation = [action: 'save', collection: 'Weather']
                weatherMap.location = place[0].location;
                weatherMap.fullName = place[0].fullName
                mongoOperation.document = weatherMap;
                def database = definedConfiguration.states[place[0].state];
                eventBus.send("${definedConfiguration.databasesAddress}.${database}", mongoOperation) { result ->
                    request.response.end("${JsonOutput.toJson(weatherMap)}")
                }
            } else {
                request.response.code = 500;
                request.response.end("{'save': 'Couldn't be saved, check location information or pollution map structure'}")
            }
        }
    } as groovy.lang.Closure

    def findWeatherByLatLon = { request ->
        def fastEagleService = definedConfiguration.fastEagleService
        String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.longitudeLatitudeService
        url = url.replace(":latitude", "$request.params.latitude")
        url = url.replace(":longitude", "$request.params.longitude")
        url = url.replace(":maxDistance", "${request.params.max ?: 100}")
        def coordinates = [Double.parseDouble(request.params.longitude ?: "0"), Double.parseDouble(request.params.latitude ?: "0")]
        def maxDistance = Double.parseDouble(request.params.max ?: "100")
        def urlObject = new URL(url)
        def place = new JsonSlurper().parse(urlObject)

        request.response.putHeader("Content-Type", "application/json")

        if (place) {
            def query = [
                    action : 'find', collection: 'Weather',
                    matcher: [
                            location: [
                                    '$near': [
                                            '$geometry'   : [type: "Point", coordinates: coordinates],
                                            '$maxDistance': maxDistance
                                    ]
                            ]
                    ]
            ]
            def database = definedConfiguration.states[place[0].state];
            eventBus.send("${definedConfiguration.databasesAddress}.${database}", query) { mongoResponse ->
                if (mongoResponse.body.results) {
                    request.response.end("${JsonOutput.toJson(mongoResponse.body.results)}")
                } else {
                    request.response.end("${JsonOutput.toJson([])}")
                }
            }
        } else {
            request.response.code = 500;
            request.response.end("{'query': 'Given place is not inside Mexico bounds'}")
        }
    } as groovy.lang.Closure

    def findWeatherByPlaceName = { request ->
        def fastEagleService = definedConfiguration.fastEagleService
        String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.nameService
        url = url.replace(":name", "$request.params.name")
        def urlObject = new URL(url)
        def place = new JsonSlurper().parse(urlObject)

        request.response.putHeader("Content-Type", "application/json")

        if (place) {
            def query = [
                    action : 'find', collection: 'Weather',
                    matcher: [
                            location: [
                                    '$near': [
                                            '$geometry': [type: "Point", coordinates: place[0].location.coordinates]
                                    ]
                            ]
                    ]
            ]
            def database = definedConfiguration.states[place[0].state];
            eventBus.send("${definedConfiguration.databasesAddress}.${database}", query) { mongoResponse ->
                if (mongoResponse.body.results) {
                    request.response.end("${JsonOutput.toJson(mongoResponse.body.results)}")
                } else {
                    request.response.end("${JsonOutput.toJson([])}")
                }

            }
        } else {
            request.response.code = 500;
            request.response.end("{'query': 'Given place not inside Mexico bounds'}")
        }
    } as groovy.lang.Closure
}
