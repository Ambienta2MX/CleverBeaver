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

    def saveWeather = { request ->
        request.bodyHandler { body ->
            def weatherMap = new JsonSlurper().parseText("$body")
            def fastEagleService = definedConfiguration.fastEagleService
            String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.longitudeLatitudeService
            url = url.replace(":latitude", "$weatherMap.latitude")
            url = url.replace(":longitude", "$weatherMap.longitude")
            url = url.replace(":distance", "${request.params.distance ?: 100}")
            def urlObject = new URL(url)
            def place = new JsonSlurper().parse(urlObject)
            if (place) {
                def mongoOperation = [action: 'save', collection: 'Weather']
                weatherMap.location = place[0].location;
                weatherMap.fullName = place[0].fullName
                mongoOperation.document = weatherMap;
                def database = definedConfiguration.states[place[0].state];
                eventBus.send("${definedConfiguration.databasesAddress}.${database}", mongoOperation) { result ->
                    request.response.putHeader("Content-Type", "application/json")
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

    def findWeatherBy = { request ->
        /*Enabling CORS*/
        request.response.putHeader("Access-Control-Allow-Origin", "${request.headers.origin}")
        request.response.putHeader("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
        request.response.putHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, Accept");
        request.response.putHeader("Content-Type", "application/json")
        def response
        try {
            if (request.params.name) {
                response = this.findWeatherByPlaceName(request)
            } else {
                response = this.findWeatherByLatLon(request)
            }
            return response
        } catch (Exception e) {
            e.printStackTrace()
            println(e.getMessage())
            println(e.getLocalizedMessage());
            return request.response.end("{'error' : ${e.getLocalizedMessage()}")
        }
    }

    def findWeatherByLatLon = { request ->
        def fastEagleService = definedConfiguration.fastEagleService
        String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.longitudeLatitudeService
        url = url.replace(":latitude", "$request.params.latitude")
        url = url.replace(":longitude", "$request.params.longitude")
        url = url.replace(":distance", "100") // for search purposes
        def coordinates = [Double.parseDouble(request.params.longitude ?: "0"), Double.parseDouble(request.params.latitude ?: "0")]
        def maxDistance = Integer.parseInt(request.params.distance ?: "100")
        def maxItems = Integer.parseInt(request.params.max ?: "10")
        def urlObject = new URL(url)
        def place = new JsonSlurper().parse(urlObject)
        if (place) {
            def query = [
                    action    : 'find', collection: 'Weather',
                    matcher   : [
                            location: [
                                    '$near': [
                                            '$geometry'   : [type: "Point", coordinates: coordinates],
                                            '$maxDistance': maxDistance
                                    ]
                            ]
                    ],
                    limit     : maxItems,
                    sort_query: [sampleDate: -1]
            ]
            eventBus.send("${definedConfiguration.WeatherFinder.address}", query) { message ->
                println("Resolving Information from $coordinates")
                if (message.body) {
                    request.response.end("${JsonOutput.toJson(message.body.results)}")
                } else {
                    request.response.end("${JsonOutput.toJson([])}")
                }
            }
        }
    } as groovy.lang.Closure

    def findWeatherByPlaceName = { request ->
        def fastEagleService = definedConfiguration.fastEagleService
        String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.nameService
        url = url.replace(":name", "${URLEncoder.encode(request.params.name)}")
        if (request.params.strict) {
            url = url + "&strict=true"
        }
        def urlObject = new URL(url)
        def maxItems = Integer.parseInt(request.params.max ?: "1")
        def maxDistance = Integer.parseInt(request.params.distance ?: "1000")
        def place = new JsonSlurper().parse(urlObject)
        def coordinates = [place[0].location.coordinates[0], place[0].location.coordinates[1]]
        if (place) {
            def query = [
                    action    : 'find', collection: 'Weather',
                    matcher   : [
                            location: [
                                    '$near': [
                                            '$geometry'   : [type: "Point", coordinates: coordinates],
                                            '$maxDistance': maxDistance
                                    ]
                            ]
                    ],
                    limit     : maxItems,
                    sort_query: [sampleDate: -1]
            ]

            eventBus.send("${definedConfiguration.WeatherFinder.address}", query) { message ->
                println("Resolving Information from $coordinates, using place name")
                if (message.body) {
                    request.response.end("${JsonOutput.toJson(message.body.results)}")
                } else {
                    request.response.end("${JsonOutput.toJson([])}")
                }
            }
        }
    } as groovy.lang.Closure
}
