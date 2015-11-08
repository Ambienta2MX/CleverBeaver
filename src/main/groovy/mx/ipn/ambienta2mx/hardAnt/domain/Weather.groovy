package mx.ipn.ambienta2mx.hardAnt.domain

/**
 * Created by alberto on 16/10/15.
 */
@groovy.transform.Canonical
class Weather{
    Date weatherTime
    String description
    Double precipIntensity
    Double precipProbability
    Double temperature
    Double apparentTemperature
    Double humidity
    Double windSpeed
    Double windBearing
    Double visibility
    Double cloudCover
    Double pressure
}