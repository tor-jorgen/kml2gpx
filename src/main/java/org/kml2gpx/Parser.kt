package org.kml2gpx

import org.kml2gpx.model.Root
import org.xml.sax.InputSource
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

fun parseKml(inputStream: InputStream?, mergeTracks: Boolean, mergeFolders: Boolean, addWaypoints: Boolean): Root {
    val parser = SAXParserFactory.newInstance().newSAXParser()
    val reader = parser.xmlReader
    val handler = KmlHandler(mergeTracks = mergeTracks, mergeFolders = mergeFolders, addWaypoints = addWaypoints)
    reader.contentHandler = handler
    reader.parse(InputSource(inputStream))
    return handler.route()
}
