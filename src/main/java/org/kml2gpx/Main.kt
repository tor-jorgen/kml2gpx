package org.kml2gpx

import org.dom4j.DocumentHelper
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import org.kml2gpx.model.Root
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty() || args[0] == "-h" || args[0] == "--help") {
        help()
        return
    }

    val arguments = parseArguments(args)
    try {
        convert(arguments)
    } catch (e: Exception) {
        println("KmlToGpx exited because of failure!")
        exitProcess(1)
    }
}

private fun parseArguments(args: Array<String>): Arguments {
    val arguments = Arguments(kmlPath = args[0])
    var skip = false
    for (i in 1..args.size - 1) {
        if (skip) {
            skip = false
            continue
        }

        when (args[i]) {
            "-o", "--output" -> {
                if (args.size <= i + 1) {
                    println("ERROR: Missing path to output file")
                    help()
                    exitProcess(1)
                } else {
                    arguments.outputPath = args[i + 1]
                    // Skip next argument, which should be path to output file
                    skip = true
                }
            }

            "-t", "--merge-tracks" -> arguments.mergeTracks = true
            "-f", "--merge-folders" -> arguments.mergeFolders = true
            "-w", "--waypoints" -> arguments.addWaypoints = true
            "-v", "--validate" -> arguments.validate = true
            "-p", "--prettify" -> arguments.prettify = true

            else -> {
                println("ERROR: Invalid argument: ${args[i]}")
                help()
                exitProcess(1)
            }
        }
    }
    return arguments
}

private fun help() {
    println(
        """Usage: java -jar [path]kml2gpx-<version>-all.jar <KML file> [options]
           |KML 2 GPX, a utility that converts a KML v. 2.2 file to a GPX v. 1.1 file.
           |
           |
           |Arguments:
           | KML file                                   Path to the KML file to convert (with file extension)
           | options:
           |  -t, --merge-tracks                        Merge all tracks (Placemarks) in a folder into one track. This will result in one track for each folder. Useful if you had to divide a single track into multiple tracks in the KML file
           |  -f, --merge-folders                       Merge all tracks (Placemarks) in all folders into one track. This will result in one track
           |  -w, --add-waypoints                       Add all waypoints to a single list. If not specified, no waypoints will be added
           |  -o, --output        <path to output file> Write the result to the given file (with file extension). If not specified, the output will be written to the console 
           |  -v, --validate                            Validate that the output is according to GPX specification
           |  -p, --prettify                            Prettify the output
        """.trimMargin(),
    )
}

private fun assertFileExists(path: String) {
    if (!Files.exists(Path.of(path))) {
        val message = "File '$path' does not exist!"
        println(message)
        throw RuntimeException(message)
    }
}

private fun convert(args: Arguments) {
    println("Converting '${args.kmlPath}' from KML to GPX. Merge tracks: ${args.mergeTracks}, merge folders: ${args.mergeFolders}, add waypoints: ${args.addWaypoints}")
    assertFileExists(args.kmlPath)
    val kml = FileInputStream(args.kmlPath)
    convert(
        kml = kml,
        mergeTracks = args.mergeTracks,
        mergeFolders = args.mergeFolders,
        addWaypoints = args.addWaypoints,
        outputPath = args.outputPath,
        validate = args.validate,
        prettify = args.prettify
    )
}

fun convert(
    kml: InputStream,
    mergeTracks: Boolean = false,
    mergeFolders: Boolean = false,
    addWaypoints: Boolean = false,
    outputPath: String? = null,
    validate: Boolean = false,
    prettify: Boolean = false,
): Root {
    val root =
        parseKml(inputStream = kml, mergeTracks = mergeTracks, mergeFolders = mergeFolders, addWaypoints = addWaypoints)
    var gpx = root.toGpx()
    if (prettify) {
        gpx = prettify(gpx)
    }
    if (validate) {
        validateGpx(gpx)
    }
    if (outputPath != null) {
        File(outputPath).writeText(gpx)
        println("* Wrote GPX file to: '$outputPath'")
    } else {
        println(gpx)
    }
    return root
}

private fun prettify(xmlString: String) =
    try {
        val format = OutputFormat.createPrettyPrint()
        format.setIndentSize(4)
        format.encoding = "UTF-8"
        val document: org.dom4j.Document = DocumentHelper.parseText(xmlString)
        val sw = StringWriter()
        val writer = XMLWriter(sw, format)
        writer.write(document)
        println("* Prettified output")
        sw.toString()
    } catch (e: Exception) {
        println("ERROR: Could not prettify output. Using original output")
        xmlString
    }
