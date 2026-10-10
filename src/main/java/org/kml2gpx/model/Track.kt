package org.kml2gpx.model

import org.kml2gpx.toCData

data class Track(
    var name: String? = null,
    var number: Int? = null,
    var description: String? = null,
    val segments: MutableList<TrackSegment> = mutableListOf()
) {
    fun addSegment(segment: TrackSegment) {
        this.segments.add(segment)
    }

    fun toGpx() =
        """
        |<trk>
        |    <name>${name.toCData()}</name>
        |    <desc>${description.toCData()}</desc>
        |    <number>${number ?: ""}</number>
        |    ${segments.stream().map { it.toGpx() }.toList().joinToString(separator = "\n")}
        |</trk>
        """.trimMargin()
}
