package org.kml2gpx

import java.net.URL
import javax.xml.XMLConstants
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

fun validateGpx(gpx: String) {
    val strictGpx = gpx.replace(GPX_SCHEMA_LOCATION, GPX_STRICT_SCHEMA_LOCATION)
    val factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    val schemaFile: Source = StreamSource(URL(GPX_STRICT_SCHEMA_LOCATION).openStream())
    val schema: Schema = factory.newSchema(schemaFile)
    val validator = schema.newValidator()
    try {
        validator.validate(StreamSource(strictGpx.byteInputStream()))
        println("* GPX validation: OK")
    } catch (e: Exception) {
        println("* GPX validation: Failed")
        throw e
    }
}
