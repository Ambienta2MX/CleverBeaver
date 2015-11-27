package mx.ipn.ambienta2mx.hardAnt

import mx.ipn.ambienta2mx.hardAnt.services.PollutionService
import mx.ipn.ambienta2mx.hardAnt.services.WeatherService
import mx.ipn.ambienta2mx.hardAnt.services.api.FileManagement
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import java.lang.Void as Should

/**
 * Created by alberto on 26/11/15.
 */
class FileManagementSpec extends Specification {
    @Shared service = null
    Should "generate a csv file from given object's array (Weather)"() {
        given:
        service = (FileManagement) new WeatherService()
        expect:
        service.generateCSVFile(array).text == fileContent
        where:
        array                                                          || fileContent
        [[latitude: 10, longitude: 10], [latitude: 11, longitude: 12]] || "latitude,longitude\n10,10\n11,12\n"
    }
    
    Should "generate a csv file from given object's array (Pollution)"() {
        given:
        service = (FileManagement) new PollutionService()
        expect:
        service.generateCSVFile(array).text == fileContent
        where:
        array                                                          || fileContent
        [[latitude: 10, longitude: 10], [latitude: 11, longitude: 12]] || "latitude,longitude\n10,10\n11,12\n"
    }
}
