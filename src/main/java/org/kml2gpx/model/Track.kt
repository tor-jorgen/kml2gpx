package org.kml2gpx.model

data class Track(
    var name: String? = null,
    var number: Int? = null,
    var description: String? = null,
    val segments: MutableList<TrackSegment> = mutableListOf()
) {
    fun addSegment(segment: TrackSegment) {
        this.segments.add(segment)
    }

    fun addSegments(segments: List<TrackSegment>) {
        this.segments.addAll(segments)
    }

    fun toGpx() =
        """
        |<trk>
        |    <name><![CDATA[${name ?: ""}]]></name>
        |    <desc><![CDATA[[${description ?: ""}]]></desc>
        |    <number>${number ?: ""}</number>
        |    ${segments.stream().map { it.toGpx() }.toList().joinToString(separator = "\n")}
        |</trk>
        """.trimMargin()
}
