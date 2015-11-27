package mx.ipn.ambienta2mx.hardAnt.services

import mx.ipn.ambienta2mx.hardAnt.services.api.FileManagement
import mx.ipn.ambienta2mx.hardAnt.verticles.routes.WeatherRouter

/**
 * Created by alberto on 16/10/15.
 */
class WeatherService implements FileManagement{
    @Override
    def generateJsonFile() {
        return null
    }

    @Override
    def generateCSVFile() {
        return null
    }
}
