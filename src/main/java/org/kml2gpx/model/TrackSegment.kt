package org.kml2gpx.model

data class TrackSegment(
    var name: String? = null,
    var number: Int? = null,
    var description: String? = null,
    val points: MutableList<Point> = mutableListOf(),
) {
    fun addPoint(point: Point) {
        this.points.add(point)
    }

    fun addPoints(points: List<Point>) {
        this.points.addAll(points)
    }

    fun toGpx() =
        """
        |<trkseg>
        |    ${points.stream().map { it.toGpx() }.toList().joinToString(separator = "\n")}
        |</trkseg>
        """.trimMargin()
}
