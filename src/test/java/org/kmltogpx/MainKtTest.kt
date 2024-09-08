package org.kmltogpx

import org.junit.jupiter.api.Test

class MainKtTest {
    @Test
    fun `Convert KML to GPX`() {
        val kml = {}.javaClass.getResource("/test2.kml")?.openStream()!!

        convert(kml = kml, outputPath = "C:/Users/torjo/Downloads/test2.gpx")
    }
}
