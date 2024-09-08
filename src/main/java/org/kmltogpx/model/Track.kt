package org.kmltogpx.model

import kotlin.streams.toList

data class Track(
    var name: String? = null,
    var description: String? = null,
) {
    private val points = mutableListOf<Point>()

    fun addPoint(point: Point) {
        this.points.add(point)
    }

    fun addPoints(points: List<Point>) {
        this.points.addAll(points)
    }

    fun toGpx() =
        """
        |<trk>
        |    <name>
        |        <![CDATA[${name ?: ""}]]></name>
        |    <desc><![CDATA[[${description ?: ""}]]></desc>
        |    <number>1</number>
        |    <trkseg>
        |        ${points.stream().map { it.toGpx() }.toList().joinToString(separator = "\n")}
        |    </trkseg>
        |</trk>
        """.trimMargin()
}
