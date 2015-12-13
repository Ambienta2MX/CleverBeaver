package mx.ipn.ambienta2mx.hardAnt.verticles.routes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import mx.ipn.ambienta2mx.hardAnt.services.DataTypeService

/**
 * Created by alberto on 16/10/15.
 */
class DataTypeRouter {
    def definedConfiguration
    def container
    def eventBus

    def saveModel = { request ->
        request.bodyHandler { body ->
            def modelMap = new JsonSlurper().parseText("$body")
            def fastEagleService = definedConfiguration.fastEagleService
            String url = fastEagleService.host + ":" + fastEagleService.port + fastEagleService.longitudeLatitudeService
            url = url.replace(":latitude", "$modelMap.latitude")
            url = url.replace(":longitude", "$modelMap.longitude")
            url = url.replace(":distance", "${request.params.distance ?: 100}")
            def urlObject = new URL(url)
            def place = new JsonSlurper().parse(urlObject)
            if (place) {
                def mongoOperation = [action: 'save', collection: request.params.dataType.capitalize()]
                modelMap.location = place[0].location;
                modelMap.fullName = place[0].fullName
                modelMap.remove("latitude")
                modelMap.remove("longitude")
                mongoOperation.document = modelMap;
                def database = definedConfiguration.states[place[0].state];
                eventBus.send("${definedConfiguration.databasesAddress}.${database}", mongoOperation) { result ->
                    request.response.putHeader("Content-Type", "application/json")
                    if (result) {
                        request.response.end("${JsonOutput.toJson(modelMap)}")
                    } else {
                        request.response.code = 404;
                        request.response.end("{'save':false}")
                    }

                }
            }
        }
    }

    def findDataTypeBy = { request ->
        /*Enabling CORS*/
        request.response.putHeader("Access-Control-Allow-Origin", "${request.headers.origin}")
        request.response.putHeader("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
        request.response.putHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, Accept");
        request.response.putHeader("Content-Type", "application/${request.params.format ?: "json"}")
        try {

            if (request.params.name) {
                return this.findDataTypeByPlaceName(request)
            } else {
                return this.findDataTypeByLatLon(request)
            }


        } catch (Exception e) {
            e.printStackTrace()
            println(e.getMessage())
            println(e.getLocalizedMessage());
            return request.response.end("{'error' : ${e.getLocalizedMessage()}")
        }
    }

    def findDataTypeByLatLon = { request ->
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
                    action    : 'find', collection: request.params.dataType.capitalize(),
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
            def message = [query: query, latest: request.params.latest ?: false]
            eventBus.send("${definedConfiguration.DataTypeFinder.address}", message) { dataFinderMessage ->
                println("Resolving Information from $coordinates")
                if (dataFinderMessage.body) {
                    return request.response.end("${new DataTypeService().generateResponseType(dataFinderMessage.body.results, request.params.format).text}")
                }
            }
        }
    }

    def findDataTypeByPlaceName = { request ->
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
                    action    : 'find', collection: request.params.dataType.capitalize(),
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
            def message = [query: query, latest: request.params.latest ?: false]
            eventBus.send("${definedConfiguration.DataTypeFinder.address}", message) { dataFinderMessage ->
                println("Resolving DataType Information from $coordinates, using place name")
                if (dataFinderMessage.body) {
                    return request.response.end("${new DataTypeService().generateResponseType(dataFinderMessage.body.results, request.params.format).text}")
                }
            }
        }
    }
}
