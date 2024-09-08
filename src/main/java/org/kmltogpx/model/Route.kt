package org.kmltogpx.model

import org.kmltogpx.GPX_SCHEMA_LOCATION
import org.kmltogpx.GPX_XMLNS
import kotlin.streams.toList

data class Route(
    var name: String? = null,
    var description: String? = null,
) {
    private val waypoints = mutableListOf<Point>()
    private val tracks = mutableListOf<Track>()

    fun addWaypoint(waypoint: Point) {
        this.waypoints.add(waypoint)
    }

    fun addWaypoints(waypoints: List<Point>) {
        this.waypoints.addAll(waypoints)
    }

    fun addTrack(track: Track) {
        this.tracks.add(track)
    }

    fun waypoints() = waypoints

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
        |    ${tracks.stream().map { it.toGpx() }.toList().joinToString(separator = "\n")}
        |</gpx>
        """.trimMargin()
}
