package mx.ipn.ambienta2mx.hardAnt.verticles.routes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import mx.ipn.ambienta2mx.hardAnt.services.PollutionService

/**
 * Created by alberto on 16/10/15.
 */
class PollutionRouter {
    def definedConfiguration
    def container
    def eventBus


    def savePollutionByLatLon =  { request ->
        request.bodyHandler { body ->
            def pollutionMap = new JsonSlurper().parseText("$body")
            def fastEagleService = definedConfiguration.fastEagleService
            String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.longitudeLatitudeService
            url = url.replace(":latitude", "$pollutionMap.latitude")
            url = url.replace(":longitude", "$pollutionMap.longitude")
            url = url.replace(":maxDistance", "${request.params.maxDistance ?: 100}")
            def urlObject = new URL(url)
            def place = new JsonSlurper().parse(urlObject)
            if (place) {
                def mongoOperation = [action: 'save', collection: 'Pollution']
                pollutionMap.location = place[0].location;
                pollutionMap.fullName = place[0].fullName
                pollutionMap.remove("latitude")
                pollutionMap.remove("longitude")
                mongoOperation.document = pollutionMap;
                def database = definedConfiguration.states[place[0].state];
                eventBus.send("${definedConfiguration.databasesAddress}.${database}", mongoOperation) { result ->
                    request.response.end("${JsonOutput.toJson(result)}")
                }
            } else {
                request.response.code = 500;
                request.response.end("{'save': 'Couldn't be saved, check location information or pollution map structure'}")
            }
        }
    }

    def findPollutionBy =  { request ->
        /*Enabling CORS*/
        request.response.putHeader("Access-Control-Allow-Origin", "${request.headers.origin}");
        request.response.putHeader("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
        request.response.putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        request.response.putHeader("Content-Type", "application/json");
        if (request.params.name) {
            return this.findPollutionByPlaceName(request)
        } else {
            return this.findPollutionByLatLon(request)
        }
    }

    def findPollutionByPlaceName =  { request ->
        def fastEagleService = definedConfiguration.fastEagleService
        String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.nameService
        url = url.replace(":name", "$request.params.name")
        def urlObject = new URL(url)
        def place = new JsonSlurper().parse(urlObject)
        def maxDistance = Integer.parseInt(request.params.distance ?: "1000")
        def coordinates = [place[0].location.coordinates[0], place[0].location.coordinates[1]]
        def maxItems = Integer.parseInt(request.params.max ?: "10")
        if (place) {
            def query = [
                    action    : 'find', collection: 'Pollution',
                    matcher   : [
                            location: [
                                    '$near': [
                                            '$geometry': [type: "Point", coordinates:coordinates],
                                            '$maxDistance': maxDistance
                                    ]
                            ]
                    ],
                    limit     : maxItems,
                    sort_query: [sampleDate: -1]
            ]
            eventBus.send("${definedConfiguration.WeatherFinder.address}", query) { message ->
                println("Resolving Polluton Information from $coordinates using place name")
                if (message.body) {
                    request.response.end("${JsonOutput.toJson(message.body.results)}")
                } else {
                    request.response.end("${JsonOutput.toJson([])}")
                }
            }
        }
    }

    def findPollutionByLatLon =  { request ->
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
                    action    : 'find', collection: 'Pollution',
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
            eventBus.send("${definedConfiguration.PollutionFinder.address}", query) { message ->
                println("Resolving Pollution Information from $coordinates")
                if (message.body) {
                    request.response.end("${JsonOutput.toJson(message.body.results)}")
                } else {
                    request.response.end("${JsonOutput.toJson([])}")
                }
            }
        }
    }
}
