package mx.ipn.ambienta2mx.hardAnt

import mx.ipn.ambienta2mx.hardAnt.services.WeatherService
import spock.lang.Ignore
import spock.lang.Specification
import java.lang.Void as Should

/**
 * Created by alberto on 26/11/15.
 */
class FileManagementSpec extends Specification {

    Should "generate a csv file from given object's array"() {
        given:
        WeatherService service = new WeatherService()
        expect:
        service.generateCSVFile(array).text == fileContent
        where:
        array                                                          || fileContent
        [[latitude: 10, longitude: 10], [latitude: 11, longitude: 12]] || "latitude,longitude\n10,10\n11,12\n"
    }
    @Ignore
    Should "generate a json file from given object's array"() {
        given:
        WeatherService service = new WeatherService()
        expect:
        service.generateCSVFile(array).text == fileContent
        where:
        array                                                          || fileContent
        [[latitude: 10, longitude: 10], [latitude: 11, longitude: 12]] || """[{"latitude": 10, "longitude":10},{"latitude":"11,"longitude":12}]"""
    }
}
