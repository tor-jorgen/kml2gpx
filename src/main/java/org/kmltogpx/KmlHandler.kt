package org.kmltogpx

import org.kmltogpx.model.Point
import org.kmltogpx.model.Route
import org.kmltogpx.model.Track
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import kotlin.streams.toList

private const val DOCUMENT = "Document"
private const val NAME = "name"
private const val DESCRIPTION = "description"
private const val FOLDER = "Folder"
private const val PLACEMARK = "Placemark"
private const val LINE_STRING = "LineString"
private const val COORDINATES = "coordinates"

private enum class Tag {
    DOCUMENT,
    DOCUMENT_NAME,
    DOCUMENT_DESCRIPTION,
    DOCUMENT_FOLDER,
    DOCUMENT_FOLDER_NAME,
    DOCUMENT_FOLDER_PLACEMARK,
    DOCUMENT_FOLDER_PLACEMARK_LINE_STRING,
    DOCUMENT_FOLDER_PLACEMARK_LINE_STRING_COORDINATES,
}

/**
 * Reads KML files into a common object model.
 *
 * Set [singleTrack] to `true` if all tracks should be combined into one
 * track. `false` will keep all the tracks
 *
 * See https://developers.google.com/kml/documentation/kmlreference
 */
class KmlHandler(
    val singleTrack: Boolean = false,
) : DefaultHandler() {
    private val route = Route()
    private var currentTrack = Track()
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
                    else -> {}
                }
            }

            DESCRIPTION -> addIf(ifCurrent = Tag.DOCUMENT, add = Tag.DOCUMENT_DESCRIPTION)
            FOLDER -> {
                if (tags.last() == Tag.DOCUMENT) {
                    tags.add(Tag.DOCUMENT_FOLDER)
                    if (!singleTrack) {
                        currentTrack = Track()
                    }
                }
            }

            PLACEMARK -> addIf(ifCurrent = Tag.DOCUMENT_FOLDER, add = Tag.DOCUMENT_FOLDER_PLACEMARK)
            LINE_STRING ->
                addIf(
                    ifCurrent = Tag.DOCUMENT_FOLDER_PLACEMARK,
                    add = Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING,
                )

            COORDINATES ->
                addIf(
                    ifCurrent = Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING,
                    add = Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING_COORDINATES,
                )
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
                route.name = substring(ch, start, length)
            }

            Tag.DOCUMENT_DESCRIPTION -> {
                val description = substring(ch, start, length)
                route.description = description
            }

            Tag.DOCUMENT_FOLDER_NAME -> {
                currentTrack.name = substring(ch, start, length)
            }

            Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING_COORDINATES -> {
                val pointString = substring(ch, start, length)
                if (!pointString.isNullOrBlank()) {
                    val (points, partial) = points(partialPointString + pointString)
                    partialPointString = partial ?: ""
                    currentTrack.addPoints(points)
                }
            }

            else -> {
                // Do nothing
            }
        }
    }

    private fun points(coordinateString: String?): Pair<List<Point>, String?> {
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
                ?.map { Point.fromString(pointString = it, name = currentPoint++.toString()) }
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
            NAME -> removeLastIf(Tag.DOCUMENT_NAME, Tag.DOCUMENT_FOLDER_NAME)
            DESCRIPTION -> removeLastIf(Tag.DOCUMENT_DESCRIPTION)
            COORDINATES -> removeLastIf(Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING_COORDINATES)
            LINE_STRING -> removeLastIf(Tag.DOCUMENT_FOLDER_PLACEMARK_LINE_STRING)
            PLACEMARK -> removeLastIf(Tag.DOCUMENT_FOLDER_PLACEMARK)
            FOLDER -> {
                if (tags.last() == Tag.DOCUMENT_FOLDER) {
                    tags.removeLast()
                    if (!singleTrack) {
                        route.addTrack(currentTrack)
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
        if (singleTrack) {
            currentTrack.name = route.name
            route.addTrack(currentTrack)
            currentPoint = 0
        }
    }

    fun route() = route
}
