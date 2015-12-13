package mx.ipn.ambienta2mx.hardAnt.services.api

/**
 * Created by alberto on 19/11/15.
 */
interface FileManagement {
    def generateJsonFile(List array);
    def generateCSVFile(List array);
    def generateXMLFile(List array);
}