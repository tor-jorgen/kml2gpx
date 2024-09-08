package org.kmltogpx

import org.kmltogpx.model.Route
import org.xml.sax.InputSource
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

fun parseKml(inputStream: InputStream?): Route {
    val parser = SAXParserFactory.newInstance().newSAXParser()
    val reader = parser.xmlReader
    val handler = KmlHandler(singleTrack = false)
    reader.contentHandler = handler
    reader.parse(InputSource(inputStream))
    return handler.route()
}
