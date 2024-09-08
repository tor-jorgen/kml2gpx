package org.kml2gpx

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.kml2gpx.model.*

private val track1 = Track(
    name = "Folder 1 name - Placemark 1.1 name",
    number = 1,
    description = "Placemark 1.1 description",
    segments = mutableListOf(
        TrackSegment(
            number = 1,
            points = mutableListOf(
                Point(
                    longitude = 10.84154451,
                    latitude = 60.60394129,
                    elevation = 505.221244140625,
                    name = "1",
                    type = PointType.TrackPoint,
                ),
                Point(
                    longitude = 10.987773744,
                    latitude = 60.486145613,
                    elevation = 293.076737304688,
                    name = "10",
                    type = PointType.TrackPoint,
                ),
            )
        ),
        TrackSegment(
            number = 3,
            points = mutableListOf(
                Point(
                    longitude = 10.946995085,
                    latitude = 60.59310953,
                    elevation = 471.379355712891,
                    name = "1",
                    type = PointType.TrackPoint,
                ),
                Point(
                    longitude = 10.9278318,
                    latitude = 60.6104059,
                    elevation = 432.879447265625,
                    name = "10",
                    type = PointType.TrackPoint,
                ),
            )
        ),
    )
)
private val track2 = Track(
    name = "Folder 1 name - Placemark 1.2 name",
    number = 2,
    description = "Placemark 1.2 description",
    segments = mutableListOf(
        TrackSegment(
            number = 1,
            points = mutableListOf(
                Point(
                    longitude = 10.6315589,
                    latitude = 60.7658567,
                    elevation = 267.029837890625,
                    name = "1",
                    type = PointType.TrackPoint,
                ),
                Point(
                    longitude = 10.421106001,
                    latitude = 60.77229864,
                    elevation = 530.560038085938,
                    name = "10",
                    type = PointType.TrackPoint,
                ),
            )
        ),
        TrackSegment(
            number = 2,
            points = mutableListOf(
                Point(
                    longitude = 10.421106001,
                    latitude = 60.77229864,
                    elevation = 530.560038085938,
                    name = "1",
                    type = PointType.TrackPoint,
                ),
                Point(
                    longitude = 10.41065234,
                    latitude = 60.79402631,
                    elevation = 521.387259765625,
                    name = "10",
                    type = PointType.TrackPoint,
                ),
            )
        ),
    )
)
private val track3 = Track(
    name = "Folder 1 name - Placemark 1.3 name",
    number = 3,
    description = "Placemark 1.3 description",
    segments = mutableListOf(
        TrackSegment(
            number = 1,
            points = mutableListOf(
                Point(
                    longitude = 10.53362554,
                    latitude = 60.81612983,
                    elevation = 466.285331054688,
                    name = "1",
                    type = PointType.TrackPoint,
                ),
                Point(
                    longitude = 10.5808439,
                    latitude = 60.81512979,
                    elevation = 447.040091796875,
                    name = "10",
                    type = PointType.TrackPoint,
                ),
            )
        ),
    )
)
private val track4 = Track(
    name = "Folder 2 name - Placemark 2.1 name",
    number = 4,
    description = "Placemark 2.1 description",
    segments = mutableListOf(
        TrackSegment(
            number = 1,
            points = mutableListOf(
                Point(
                    longitude = 12.5134921073914,
                    latitude = 60.2558469772339,
                    elevation = 250.379,
                    name = "1",
                    type = PointType.TrackPoint,
                ),
                Point(
                    longitude = 12.5064325332642,
                    latitude = 60.2617907524109,
                    elevation = 263.239,
                    name = "10",
                    type = PointType.TrackPoint,
                ),
            )
        ),
    )
)
private val track5 = Track(
    name = "Folder 2 name - Placemark 2.2 name",
    number = 5,
    description = "Placemark 2.2 description",
    segments = mutableListOf(
        TrackSegment(
            number = 1,
            points = mutableListOf(
                Point(
                    longitude = 9.28659458644688,
                    latitude = 61.8972507677972,
                    elevation = 0.0,
                    name = "1",
                    type = PointType.TrackPoint,
                ),
                Point(
                    longitude = 9.28915500640869,
                    latitude = 61.8982815742493,
                    elevation = 0.0,
                    name = "10",
                    type = PointType.TrackPoint,
                ),
            )
        ),
    )
)

//TODO: Test description (and name) with LF and CDATA
class MainKtTest {
    @Test
    fun `Convert KML to GPX with no merge`() {
        val kml = {}.javaClass.getResource("/multi-folder.kml")?.openStream()!!
        val root = convert(kml = kml, outputPath = "build/multi-folder.gpx", validate = true, prettify = true)
        assertEquals("Document name", root.name)
        assertEquals("Document description", root.description)
        assertEquals(5, root.tracks().size)
        evaluateTrack(root, track1, trackNo = 0, numSegments = 3)
        evaluateTrack(root, track2, trackNo = 1, numSegments = 2)
        evaluateTrack(root, track3, trackNo = 2, numSegments = 1)
        evaluateTrack(root, track4, trackNo = 3, numSegments = 1)
        evaluateTrack(root, track5, trackNo = 4, numSegments = 1)
    }

    private fun evaluateTrack(root: Root, track: Track, trackNo: Int, numSegments: Int) {
        assertEquals(track.name, root.tracks()[trackNo].name)
        assertEquals(track.description, root.tracks()[trackNo].description)
        assertEquals(track.number, root.tracks()[trackNo].number)
        assertEquals(numSegments, root.tracks()[trackNo].segments.size)
        assertEquals(track.segments.first().number, root.tracks()[trackNo].segments.first().number)
        assertEquals(track.segments.first().points.first(), root.tracks()[trackNo].segments.first().points.first())
        assertEquals(track.segments.first().points.last(), root.tracks()[trackNo].segments.first().points.last())
        assertEquals(track.segments.last().number, root.tracks()[trackNo].segments.last().number)
        assertEquals(track.segments.last().points.first(), root.tracks()[trackNo].segments.last().points.first())
        assertEquals(track.segments.last().points.last(), root.tracks()[trackNo].segments.last().points.last())
    }

    @Test
    fun `Convert KML to GPX with merge tracks`() {
        val kml = {}.javaClass.getResource("/multi-folder.kml")?.openStream()!!
        val root =
            convert(
                kml = kml,
                outputPath = "build/multi-folder-merge-tracks.gpx",
                validate = true,
                prettify = true,
                mergeTracks = true
            )

        assertEquals("Document name", root.name)
        assertEquals("Document description", root.description)
        assertEquals(2, root.tracks().size)
        assertEquals(6, root.tracks()[0].segments.size)
        assertEquals("Folder 1 name", root.tracks()[0].name)
        assertNull(root.tracks()[0].description)
        assertEquals(2, root.tracks()[1].segments.size)
        assertEquals("Folder 2 name", root.tracks()[1].name)
        assertNull(root.tracks()[1].description)
        assertEquals(1, root.tracks()[0].segments.first().number)
        assertEquals(track1.segments.first().points.first(), root.tracks()[0].segments.first().points.first())
        assertEquals(track1.segments.first().points.last(), root.tracks()[0].segments.first().points.last())
        assertEquals(6, root.tracks()[0].segments.last().number)
        assertEquals(track3.segments.last().points.first(), root.tracks()[0].segments.last().points.first())
        assertEquals(track3.segments.last().points.last(), root.tracks()[0].segments.last().points.last())
        assertEquals(1, root.tracks()[1].segments.first().number)
        assertEquals(track4.segments.first().points.first(), root.tracks()[1].segments.first().points.first())
        assertEquals(track4.segments.first().points.last(), root.tracks()[1].segments.first().points.last())
        assertEquals(2, root.tracks()[1].segments.last().number)
        assertEquals(track5.segments.last().points.first(), root.tracks()[1].segments.last().points.first())
        assertEquals(track5.segments.last().points.last(), root.tracks()[1].segments.last().points.last())
    }

    @Test
    fun `Convert KML to GPX with merge folders`() {
        val kml = {}.javaClass.getResource("/multi-folder.kml")?.openStream()!!
        val root =
            convert(
                kml = kml,
                outputPath = "build/multi-folder-merge-folders.gpx",
                validate = true,
                prettify = true,
                mergeFolders = true
            )

        assertEquals("Document name", root.name)
        assertEquals("Document description", root.description)
        assertEquals(1, root.tracks().size)
        assertEquals(8, root.tracks()[0].segments.size)
        assertEquals("Document name", root.tracks()[0].name)
        assertEquals("Document description", root.tracks()[0].description)
        assertEquals(1, root.tracks()[0].number)
        assertEquals(1, root.tracks()[0].segments.first().number)
        assertEquals(track1.segments.first().points.first(), root.tracks()[0].segments.first().points.first())
        assertEquals(track1.segments.first().points.last(), root.tracks()[0].segments.first().points.last())
        assertEquals(8, root.tracks()[0].segments.last().number)
        assertEquals(track5.segments.last().points.first(), root.tracks()[0].segments.last().points.first())
        assertEquals(track5.segments.last().points.last(), root.tracks()[0].segments.last().points.last())
    }

    @Test
    fun `Convert KML to GPX with waypoints`() {
        val kml = {}.javaClass.getResource("/waypoints.kml")?.openStream()!!
        val root = convert(
            kml = kml,
            outputPath = "build/waypoints.gpx",
            addWaypoints = true,
            validate = true,
            prettify = true
        )
        val firstPoint =
            Point(
                longitude = 9.4441693,
                latitude = 59.7678652,
                elevation = 0.0,
                name = "Directions from Nipeto, Svene to Vestbyveien 14, 3330 Skotselv, Norway - Blestølen Pub og Café, Flesberg",
                type = PointType.WayPoint,
            )
        val lastPoint =
            Point(
                longitude = 10.0525355,
                latitude = 59.7516179,
                elevation = 0.0,
                name = "Directions from Vestbyveien 14, 3330 Skotselv, Norway to Spikkestad - Drammensveien 138, 3050 Mjøndalen, Norway",
                type = PointType.WayPoint,
            )

        assertEquals(12, root.waypoints().size)
        assertEquals(firstPoint, root.waypoints().first())
        assertEquals(lastPoint, root.waypoints().last())
    }
}
