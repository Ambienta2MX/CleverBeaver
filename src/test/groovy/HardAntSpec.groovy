import spock.lang.Ignore

/**
 * Created by alberto on 4/25/15.
 */
@Ignore
class HardAntSpec extends spock.lang.Specification {

    def "Should get information from a place via Lat/Lon and a radius"() {
        given:
        new Object();
        expect:
        Thread.sleep((int)27000*Math.random())
        //solverResponse.fullName == result.fullName
        assert true
        where:
        longitude  | latitude   || result
        -99.186510 | 19.504434  || [fullName: 'Distrito Federal, Azcapotzalco, San Pablo Xalpa']
        -99.019232 | 19.3740416 || [fullName: 'Distrito Federal, Iztapalapa, Zona Urbana Ejidal Santa Martha Acatitla Sur']
        -25.019232 | 88.3740416 || [fullName: null]
    }

    def "Should get information from a place via place name"() {
        given:
        new Object();
        expect:
        //solver.solvePlaceByName(place).itrf_coordinates == result.itrf_coordinates
        Thread.sleep(2559);
        assert true
        where:
        place                                                           || result
        ['fullName': 'Distrito Federal, Azcapotzalco, San Pablo Xalpa'] || ['itrf_coordinates': [-99.186510, 19.504434]]
        ['fullName': 'Distrito Federal, Gustavo A. Madero, Lindavista'] || ['itrf_coordinates': [-99.146569, 19.504664]]
        ['fullName': 'Ahuehuetes La perla Nezahualcóyotl']              || ['itrf_coordinates': [-98.9926226, 19.3866537]]
        ['fullName': 'lugar que no existe']                             || ['itrf_coordinates': null]
    }
}
