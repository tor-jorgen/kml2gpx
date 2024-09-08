package org.kml2gpx.model

import org.kml2gpx.GPX_SCHEMA_LOCATION
import org.kml2gpx.GPX_XMLNS

data class Root(
    var name: String? = null,
    var description: String? = null,
) {
    private val waypoints = mutableListOf<Point>()
    private val tracks = mutableListOf<Track>()

    fun addWaypoints(waypoints: List<Point>) {
        this.waypoints.addAll(waypoints)
    }

    fun addTrack(track: Track) {
        this.tracks.add(track)
    }

    fun waypoints() = waypoints

    fun tracks() = tracks

    fun toGpx() =
        """
        |<?xml version="1.0" standalone="yes"?>
        |<gpx xmlns="$GPX_XMLNS" creator="kml2gpx" version="1.1"
        |     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |     xsi:schemaLocation="$GPX_XMLNS $GPX_SCHEMA_LOCATION">
        |    <metadata>
        |        <name><![CDATA[${name ?: ""}]]></name>
        |        <desc><![CDATA[${description ?: ""}]]></desc>
        |    </metadata>
        |    ${waypoints.stream().map { it.toGpx() }.toList().joinToString(separator = "\n")}
        |    ${tracks.stream().map { it.toGpx() }.toList().joinToString(separator = "\n")}
        |</gpx>
        """.trimMargin()
}
