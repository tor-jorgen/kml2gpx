package org.kml2gpx

import org.kml2gpx.model.*
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

private const val DOCUMENT = "Document"
private const val NAME = "name"
private const val DESCRIPTION = "description"
private const val FOLDER = "Folder"
private const val PLACEMARK = "Placemark"
private const val LINE_STRING = "LineString"
private const val POINT = "Point"
private const val COORDINATES = "coordinates"

private enum class Tag {
    DOCUMENT,
    DOCUMENT_NAME,
    DOCUMENT_DESCRIPTION,
    DOCUMENT_FOLDER,
    DOCUMENT_FOLDER_NAME,
    DOCUMENT_FOLDER_PLACEMARK,
    DOCUMENT_FOLDER_PLACEMARK_NAME,
    DOCUMENT_FOLDER_PLACEMARK_DESCRIPTION,
    DOCUMENT_FOLDER_PLACEMARK_LINE_STRING,
    DOCUMENT_FOLDER_PLACEMARK_LINE_STRING_COORDINATES,
    DOCUMENT_FOLDER_PLACEMARK_POINT,
    DOCUMENT_FOLDER_PLACEMARK_POINT_COORDINATES,
}

/**
 * Reads KML files into a common object model.
 *
 * Set [mergeTracks] to `true` if all tracks in a folder should be merged into one track. `false` will keep all the tracks
 *
 * Set [mergeFolders] to `true` if all tracks in all folders should be merged into one track. `false` will keep all the tracks
 *
 * See https://developers.google.com/kml/documentation/kmlreference
 */
class KmlHandler(
    val mergeTracks: Boolean = false,
    val mergeFolders: Boolean = false,
    val addWaypoints: Boolean = false,
) : DefaultHandler() {
    private val root = Root()
    private var currentFolderName: String? = null
    private var currentTrack: Track? = null
    private var currentSegment: TrackSegment? = null
    private var currentPoint = 1
    private var partialPointString: String = ""

    private val tags = mutableListOf<Tag>()

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
    ) {
        when (qName) {
            DOCUMENT -> tags.add(Tag.DOCUMENT)
            NAME -> {
                when (tags.last()) {
                    Tag.DOCUMENT -> tags.add(Tag.DOCUMENT_NAME)
                    Tag.DOCUMENT_FOLDER -> tags.add(Tag.DOCUMENT_FOLDER_NAME)
                    Tag.DOCUMENT_FOLDER_PLACEMARK -> tags.add(Tag.DOCUMENT_FOLDER_PLACEMARK_NAME)
                    else -> {}
                }
            }

            DESCRIPTION -> {
                when (tags.last()) {
                    Tag.DOCUMENT -> tags.add(Tag.DOCUMENT_DESCRIPTION)
                    Tag.DOCUMENT_FOLDER_PLACEMARK -> tags.add(Tag.DOCUMENT_FOLDER_PLACEMARK_DESCRIPTION)
                    else -> {}
                }
            }

            FOLDER -> {
                if (tags.last() == Tag.DOCUMENT) {
                    tags.add(Tag.DOCUMENT_FOLDER)
                    if (mergeTracks) {
                        currentTrack = Track(number = 1)
                    }
                    currentPoint = 1
                }
            }

            PLACEMARK -> {
                if (tags.last() == Tag.DOCUMENT_FOLDER) {
                    tags.add(Tag.DOCUMENT_FOLDER_PLACEMARK)
                    if (currentTrack == null) {
                        currentTrack = Track(number = 1)
                    } else if (!mergeTracks && !mergeFolders) {
                        currentTrack = Track(number = currentTrack?.number?.inc())
                    }
                }
            }

            LINE_STRING -> {
                if (tags.last() == Tag.DOCUMENT_FOLDER_PLACEMARK) {
                    tags.add(Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING)
                    currentSegment =
                        if (currentSegment == null) {
                            TrackSegment(number = 1)
                        } else {
                            TrackSegment(number = currentSegment?.number?.inc())
                        }
                    currentPoint = 1
                }
            }

            POINT ->
                addIf(
                    ifCurrent = Tag.DOCUMENT_FOLDER_PLACEMARK,
                    add = Tag.DOCUMENT_FOLDER_PLACEMARK_POINT,
                )

            COORDINATES -> {
                addIf(
                    ifCurrent = Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING,
                    add = Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING_COORDINATES,
                )
                addIf(
                    ifCurrent = Tag.DOCUMENT_FOLDER_PLACEMARK_POINT,
                    add = Tag.DOCUMENT_FOLDER_PLACEMARK_POINT_COORDINATES,
                )
            }
        }
    }

    private fun addIf(
        ifCurrent: Tag,
        add: Tag,
    ) {
        if (tags.last() == ifCurrent) {
            tags.add(add)
        }
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
    ) {
        if (tags.isEmpty()) return

        when (tags.last()) {
            Tag.DOCUMENT_NAME -> {
                root.name = substring(ch, start, length)
            }

            Tag.DOCUMENT_DESCRIPTION -> {
                val description = substring(ch, start, length)
                root.description = description
            }

            Tag.DOCUMENT_FOLDER_NAME -> {
                currentFolderName = substring(ch, start, length)
                if (mergeTracks) {
                    currentTrack?.name = currentFolderName
                }
            }

            Tag.DOCUMENT_FOLDER_PLACEMARK_NAME -> {
                if (!mergeTracks && !mergeFolders) {
                    currentTrack?.name = "${currentFolderName ?: ""}${if (currentFolderName != null) " - " else ""}${
                        substring(ch, start, length)
                    }"
                }
            }

            Tag.DOCUMENT_FOLDER_PLACEMARK_DESCRIPTION -> {
                if (!mergeTracks && !mergeFolders) {
                    currentTrack?.description = substring(ch, start, length)
                }
            }

            Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING_COORDINATES -> {
                val pointString = substring(ch, start, length)
                if (!pointString.isNullOrBlank()) {
                    val (points, partial) = points(
                        coordinateString = partialPointString + pointString,
                        type = PointType.TrackPoint
                    )
                    partialPointString = partial ?: ""
                    currentSegment?.addPoints(points)
                }
            }

            Tag.DOCUMENT_FOLDER_PLACEMARK_POINT_COORDINATES -> {
                if (addWaypoints) {
                    val pointString = substring(ch, start, length)
                    if (!pointString.isNullOrBlank()) {
                        val (points, _) = points(
                            coordinateString = pointString,
                            name = currentTrack?.name,
                            type = PointType.WayPoint
                        )
                        root.addWaypoints(points)
                    }
                }
            }

            else -> {
                // Do nothing
            }
        }
    }

    private fun points(coordinateString: String?, name: String? = null, type: PointType): Pair<List<Point>, String?> {
        var partial: String? = null
        val split =
            coordinateString
                ?.split(" ")
                ?.stream()
                ?.filter { !it.isNullOrBlank() }
                ?.toList()
        val points =
            split
                ?.stream()
                ?.map {
                    Point.fromString(pointString = it, name = name ?: currentPoint++.toString(), type = type)
                }
                ?.filter { !it.isEmpty }
                ?.map { it.get() }
                ?.toList() ?: emptyList()

        if (split?.size != points.size) {
            // Partial point
            partial = split?.last()
        }

        return Pair(points, partial)
    }

    private fun substring(
        ch: CharArray?,
        start: Int,
        length: Int,
        joinLines: Boolean = true,
    ): String? {
        var substring =
            ch
                ?.copyOfRange(start, start + length)
                ?.joinToString(separator = "")
                ?.trim()
        substring = if (joinLines) substring?.replace("\n", "") else substring
        return if (substring.isNullOrBlank()) null else substring
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
    ) {
        when (qName) {
            NAME -> removeLastIf(Tag.DOCUMENT_NAME, Tag.DOCUMENT_FOLDER_NAME, Tag.DOCUMENT_FOLDER_PLACEMARK_NAME)
            DESCRIPTION -> removeLastIf(Tag.DOCUMENT_DESCRIPTION, Tag.DOCUMENT_FOLDER_PLACEMARK_DESCRIPTION)
            COORDINATES -> removeLastIf(
                Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING_COORDINATES,
                Tag.DOCUMENT_FOLDER_PLACEMARK_POINT_COORDINATES
            )

            LINE_STRING -> {
                if (tags.last() == Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING) {
                    // End of segment
                    tags.removeLast()
                    if (currentTrack != null && currentSegment != null) {
                        currentTrack!!.addSegment(currentSegment!!)
                    }
                }
            }

            POINT -> removeLastIf(Tag.DOCUMENT_FOLDER_PLACEMARK_POINT)
            PLACEMARK -> {  // End of track
                if (tags.last() == Tag.DOCUMENT_FOLDER_PLACEMARK) {
                    tags.removeLast()
                    if (!mergeTracks && !mergeFolders && currentTrack != null) {
                        root.addTrack(currentTrack!!)
                        currentSegment = null
                    }
                }
            }

            FOLDER -> {
                if (tags.last() == Tag.DOCUMENT_FOLDER) {
                    tags.removeLast()
                    if (mergeTracks && !mergeFolders && currentTrack != null) {
                        root.addTrack(currentTrack!!)
                        currentTrack = null
                        currentSegment = null
                    }
                }
            }
        }
    }

    private fun removeLastIf(vararg tag: Tag) {
        tag.forEach {
            if (tags.last() == it) {
                tags.removeLast()
            }
        }
    }

    override fun endDocument() {
        if (mergeFolders && currentTrack != null) {
            currentTrack?.name = root.name
            currentTrack?.description = root.description
            root.addTrack(currentTrack!!)
        }
    }

    fun route() = root
}

// No merge:
//----------
// GPX -> nothing
// Folder -> track name prefix
// Placemark -> track
// LineString -> Segment

// Merge tracks:
//--------------
// GPX -> nothing
// Folder -> track
// Placemark -> nothing
// LineString -> segment

// Merge folders:
//---------------
// GPX -> track
// Folder -> nothing
// Placemark -> nothing
// LineString -> segment
