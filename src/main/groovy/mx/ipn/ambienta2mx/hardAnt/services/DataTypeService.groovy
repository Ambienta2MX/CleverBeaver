package mx.ipn.ambienta2mx.hardAnt.services

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
        eventBus.registerHandler("$definedConfiguration.WeatherFinder.address") { message ->
            println "WeatherFinder Verticle working";
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
    def generateJsonFile(ArrayList array) {
        return null
    }

    @Override
    def generateCSVFile(ArrayList array) {
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
                values.add(element."$property" ?: " ")
            }
            csvFileContent += "${values.join(",")}\n"
        }

        return [text: csvFileContent, size: csvFileContent.length()]
    }
}
