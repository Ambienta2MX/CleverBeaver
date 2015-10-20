package mx.ipn.ambienta2mx.hardAnt.domain

/**
 * Created by alberto on 16/10/15.
 */
@groovy.transform.Canonical
class Place {
    def location //GEOJson Spec
    List sexagesimal_coordinates
    List itrf_coordinates
    List nad27_coordinates
    def height
    String town
    String state
    String city
    String fullName
    String zipCode
    List extraInfo
    List provider
    Date lastUpdated
    Date dateCreated
}
