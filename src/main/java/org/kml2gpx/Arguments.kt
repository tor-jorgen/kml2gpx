package org.kml2gpx

data class Arguments(
    val kmlPath: String,
    var outputPath: String? = null,
    var mergeTracks: Boolean = false,
    var mergeFolders: Boolean = false,
    var addWaypoints: Boolean = false,
    var validate: Boolean = false,
    var prettify: Boolean = false,
)
