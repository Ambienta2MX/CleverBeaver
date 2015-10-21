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
    def findPollutionByLatLon = { request ->
        return;
    }
}
