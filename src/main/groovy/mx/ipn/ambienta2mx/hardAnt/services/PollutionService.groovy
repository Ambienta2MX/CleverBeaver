package mx.ipn.ambienta2mx.hardAnt.services

<<<<<<< Updated upstream
import org.vertx.groovy.platform.Verticle
=======
import mx.ipn.ambienta2mx.hardAnt.services.api.FileManagement
>>>>>>> Stashed changes

/**
 * Created by alberto on 16/10/15.
 */
<<<<<<< Updated upstream
class PollutionService extends Verticle {
    Map definedConfiguration
    def eventBus

    def start() {
        definedConfiguration = container.getConfig()
        eventBus = vertx.eventBus
        eventBus.registerHandler("$definedConfiguration.WeatherFinder.address") { message ->
            println "Pollution Verticle working";
            eventBus.send("${definedConfiguration.databasesAddress}.MX1", message.body) { mx1 ->
                eventBus.send("${definedConfiguration.databasesAddress}.MX2", message.body) { mx2 ->
                    eventBus.send("${definedConfiguration.databasesAddress}.MX3", message.body) { mx3 ->
                        eventBus.send("${definedConfiguration.databasesAddress}.MX4", message.body) { mx4 ->
                            List allPlaces = []
                            for(element in mx1.body.results) {
                                allPlaces.add(element)
                            }
                            for(element in mx2.body.results) {
                                allPlaces.add(element)
                            }
                            for(element in mx3.body.results) {
                                allPlaces.add(element)
                            }
                            for(element in mx4.body.results) {
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

    def stop() {
        container.logger.error(this.class.name + "has been stopped");
    }

=======
class PollutionService implements FileManagement{

    @Override
    def generateJsonFile() {
        return null
    }

    @Override
    def generateCSVFile() {
        return null
    }
>>>>>>> Stashed changes
}
