package mx.ipn.ambienta2mx.hardAnt

import mx.ipn.ambienta2mx.hardAnt.services.DataTypeService
import mx.ipn.ambienta2mx.hardAnt.services.api.FileManagement
import spock.lang.Shared
import spock.lang.Specification
import java.lang.Void as Should

/**
 * Created by alberto on 26/11/15.
 */
class FileManagementSpec extends Specification {
    @Shared
            service = null

    Should "generate a csv file from given object's array"() {
        given:
        service = (FileManagement) new DataTypeService()
        expect:
        service.generateCSVFile(array).text == fileContent
        where:
        array                                                          || fileContent
        [[latitude: 10, longitude: 10], [latitude: 11, longitude: 12]] || "latitude,longitude\n10,10\n11,12\n"
    }

    Should "generate a xml file from given object's array "() {
        given:
        service = (FileManagement) new DataTypeService()
        expect:
        service.generateXMLFile(array).text == fileContent
        where:
        array                                                          || fileContent
        [[latitude: 10, longitude: 10], [latitude: 11, longitude: 12]] || "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<elements>\n" +
                "  <item>\n" +
                "    <latitude>10</latitude>\n" +
                "    <longitude>10</longitude>\n" +
                "  </item>\n" +
                "  <item>\n" +
                "    <latitude>11</latitude>\n" +
                "    <longitude>12</longitude>\n" +
                "  </item>\n" +
                "</elements>"
    }
}
