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
    def ignoredFields = ['_id', 'location', '$oid']

    @Override
    def start() {
        definedConfiguration = container.getConfig()
        eventBus = vertx.eventBus
        eventBus.registerHandler("$definedConfiguration.DataTypeFinder.address") { message ->
            println "DataType Finder Verticle working";
            eventBus.send("${definedConfiguration.databasesAddress}.MX1", message.body.query) { mx1 ->
                eventBus.send("${definedConfiguration.databasesAddress}.MX2", message.body.query) { mx2 ->
                    eventBus.send("${definedConfiguration.databasesAddress}.MX3", message.body.query) { mx3 ->
                        eventBus.send("${definedConfiguration.databasesAddress}.MX4", message.body.query) { mx4 ->
                            List allPlaces = []
                            allPlaces.addAll(latestFilter(mx1.body.results, message.body.latest))
                            allPlaces.addAll(latestFilter(mx2.body.results, message.body.latest))
                            allPlaces.addAll(latestFilter(mx3.body.results, message.body.latest))
                            allPlaces.addAll(latestFilter(mx4.body.results, message.body.latest))
                            message.reply([results: allPlaces]);
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

        Set keys = []
        List values = []
        for (element in array[0]) {
            if (!(element.key in ignoredFields)) {
                keys.add(element.key)
            }
            if (element.key == 'location') {
                keys.add('longitude')
                keys.add('latitude')
            }
        }
        csvFileContent = "${keys.join(",")}\n"

        for (element in array) {
            values.clear()
            for (property in keys) {
                if (property == 'latitude') {
                    values.add("\"" + element."location".coordinates[1] + "\"" ?: " ")
                } else if (property == 'longitude') {
                    values.add("\"" + element."location".coordinates[0] + "\"" ?: " ")
                } else {
                    values.add("\"" + element."${property}" + "\"" ?: " ")
                }
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
                            if (!(key in ignoredFields)) {
                                "$key" { value instanceof Map ? value.collect(owner) : mkp.yield(value) }
                            }

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

    List latestFilter(List elements, boolean enabled) {
        if (enabled) {
            use(TimeCategory) {
                Date currentDate = new Date();
                return elements.findAll { element ->
                    //TODO verify date format before saving it into the database
                    Date expirationDate = element.sampleDate + definedConfiguration.expirationHour.hours
                    return expirationDate < currentDate
                }
            }
        } else {
            return elements
        }

    }
}
