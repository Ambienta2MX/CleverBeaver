package mx.ipn.ambienta2mx.hardAnt.verticles.routes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Created by alberto on 16/10/15.
 */
class PollutionRouter {
    def definedConfiguration
    def container
    def eventBus

    def savePollutionByLatLon = { request ->
        request.bodyHandler { body ->
            def pollutionMap = new JsonSlurper().parseText("$body")
            request.response.end("${JsonOutput.toJson(pollutionMap)}")
        }
    }

    def findPollutionBy = { request ->
        if (request.params.name) {
            return this.findPollutionByPlaceName(request)
        } else {
            return this.findPollutionByLatLon(request)
        }
    }

    def findPollutionByPlaceName = { request ->
        def fastEagleService = definedConfiguration.fastEagleService
        String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.nameService
        url = url.replace(":name", "$request.params.name")
        def urlObject = new URL(url)
        def place = new JsonSlurper().parse(urlObject)
        def maxItems = Integer.parseInt(request.params.max ?: "10")
        if (place) {
            def query = [
                    action : 'find', collection: 'Pollution',
                    matcher: [
                            location: [
                                    '$near': [
                                            '$geometry': [type: "Point", coordinates: place[0].location.coordinates]
                                    ]
                            ]
                    ],
                    limit: maxItems
            ]
            def database = definedConfiguration.states[place[0].state];
            eventBus.send("${definedConfiguration.databasesAddress}.${database}", query) { mongoResponse ->
                request.response.putHeader("Content-Type", "application/json")
                if (mongoResponse.body.results) {
                    request.response.end("${JsonOutput.toJson(mongoResponse.body.results)}")
                } else {
                    request.response.end("${JsonOutput.toJson([])}")
                }

            }
        }
    }

    def findPollutionByLatLon = { request ->
        def fastEagleService = definedConfiguration.fastEagleService
        String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.longitudeLatitudeService
        url = url.replace(":latitude", "$request.params.latitude")
        url = url.replace(":longitude", "$request.params.longitude")
        url = url.replace(":distance", "${request.params.distance ?: 100}")
        def coordinates = [Double.parseDouble(request.params.longitude ?: "0"), Double.parseDouble(request.params.latitude ?: "0")]
        def maxDistance = Double.parseDouble(request.params.distance ?: "100")
        def maxItems = Integer.parseInt(request.params.max ?: "10")
        def urlObject = new URL(url)
        def place = new JsonSlurper().parse(urlObject)
        if (place) {
            def query = [
                    action : 'find', collection: 'Pollution',
                    matcher: [
                            location: [
                                    '$near': [
                                            '$geometry'   : [type: "Point", coordinates: coordinates],
                                            '$maxDistance': maxDistance
                                    ]
                            ]
                    ],
                    limit  : maxItems
            ]
            def database = definedConfiguration.states[place[0].state];
            eventBus.send("${definedConfiguration.databasesAddress}.${database}", query) { mongoResponse ->
                request.response.putHeader("Content-Type", "application/json")
                if (mongoResponse.body.results) {
                    request.response.end("${JsonOutput.toJson(mongoResponse.body.results)}")
                } else {
                    request.response.end("${JsonOutput.toJson([])}")
                }

            }
        }
    }
}
