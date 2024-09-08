package org.kml2gpx.model

import java.util.*

data class Point(
    val longitude: Double,
    val latitude: Double,
    val elevation: Double? = 0.0,
    val name: String? = null,
    val type: PointType,
) {
    companion object {
        fun fromString(
            pointString: String,
            name: String? = null,
            type: PointType,
        ): Optional<Point> {
            val parts = pointString.split(",")
            return if (parts.size != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
                Optional.empty<Point>()
            } else {
                try {
                    Optional.of(
                        Point(
                            longitude = parts[0].toDouble(),
                            latitude = parts[1].toDouble(),
                            elevation = parts[2].toDouble(),
                            name = name,
                            type = type
                        ),
                    )
                } catch (e: Exception) {
                    println("ERROR: Illegal point string: $pointString")
                    Optional.empty<Point>()
                }
            }
        }
    }

    fun toGpx() =
        """
        |<${type.gpxName} lat="$latitude" lon="$longitude">
        |    <ele>${elevation ?: ""}</ele>
        |    <name>${name ?: ""}</name>
        |</${type.gpxName}>
        """.trimMargin()
}
