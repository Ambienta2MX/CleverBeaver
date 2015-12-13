package mx.ipn.ambienta2mx.hardAnt.services

import groovy.json.JsonOutput
import groovy.time.TimeCategory
import groovy.xml.MarkupBuilder
import mx.ipn.ambienta2mx.hardAnt.services.api.FileManagement
import org.vertx.groovy.platform.Verticle

/**
 * Created by alberto on 16/10/15.
 */

class DataTypeService extends Verticle implements FileManagement {

    Map definedConfiguration
    def eventBus

    @Override
    def start() {
        definedConfiguration = container.getConfig()
        eventBus = vertx.eventBus
        eventBus.registerHandler("$definedConfiguration.DataTypeFinder.address") { message ->
            println "DataType Finder Verticle working";
            eventBus.send("${definedConfiguration.databasesAddress}.MX1", message.body) { mx1 ->
                eventBus.send("${definedConfiguration.databasesAddress}.MX2", message.body) { mx2 ->
                    eventBus.send("${definedConfiguration.databasesAddress}.MX3", message.body) { mx3 ->
                        eventBus.send("${definedConfiguration.databasesAddress}.MX4", message.body) { mx4 ->
                            List allPlaces = []
                            for (element in mx1.body.results) {
                                allPlaces.add(element)
                            }
                            for (element in mx2.body.results) {
                                allPlaces.add(element)
                            }
                            for (element in mx3.body.results) {
                                allPlaces.add(element)
                            }
                            for (element in mx4.body.results) {
                                allPlaces.add(element)
                            }
                            message.reply([results: allPlaces]);
                            //
                        }
                    }
                }
            }
        };
    }

    @Override
    def stop() {
        container.logger.error(this.class.name + "has been stopped");
    }

    @Override
    def generateJsonFile(List array) {
        String jsonFileContent = JsonOutput.toJson(array)
        return [text: jsonFileContent, size: jsonFileContent.length()]
    }

    @Override
    def generateCSVFile(List array) {
        String csvFileContent

        List keys = []
        List values = []
        for (element in array[0]) {
            keys.add(element.key)
        }
        csvFileContent = "${keys.join(",")}\n"

        for (element in array) {
            values.clear()
            for (property in keys) {
                values.add("\"" + element."${property}" + "\"" ?: " ")
            }
            csvFileContent += "${values.join(",")}\n"
        }

        return [text: csvFileContent, size: csvFileContent.length()]
    }

    @Override
    def generateXMLFile(List array) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.with {
            elements {
                array.collect { element ->
                    item {
                        element.each { key, value ->
                            "$key" { value instanceof Map ? value.collect(owner) : mkp.yield(value) }
                        }
                    }
                }
            }
        }
        String xmlFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".concat(writer.toString())
        return [text: xmlFileContent, size: xmlFileContent.length()]
    }

    def generateResponseType(List array, String type = "json") {
        Map response
        if (array) {
            if (type == "xml") {
                response = generateXMLFile(array)
            } else if (type == "csv") {
                response = generateCSVFile(array)
            } else {
                response = generateJsonFile(array)
            }
            return response
        } else {
            return [text: "", size: 0]
        }
    }
}
