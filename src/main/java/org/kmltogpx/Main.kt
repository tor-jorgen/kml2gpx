package org.kmltogpx

import org.dom4j.DocumentHelper
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

private const val OUTPUT = "OUTPUT"
private const val VALIDATE = "VALIDATE"
private const val PRETTIFY = "PRETTIFY"

fun main(args: Array<String>) {
    if (args.isEmpty() || args[0] == "-h" || args[0] == "--help") {
        help()
        return
    }

    val arguments = parseArguments(args)
    try {
        convert(
            kmlPath = args[0],
            outputPath = arguments[OUTPUT],
            validate = arguments[VALIDATE] != null,
            prettify = arguments[PRETTIFY] != null,
        )
    } catch (e: Exception) {
        println("KmlToGpx exited because of failure!")
        exitProcess(1)
    }
}

private fun parseArguments(args: Array<String>): MutableMap<String, String?> {
    val arguments =
        mutableMapOf<String, String?>(
            OUTPUT to null,
            VALIDATE to null,
            PRETTIFY to null,
        )
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
                    arguments[OUTPUT] = args[i + 1]
                    // Skip next argument, which should be path to output file
                    skip = true
                }
            }

            "-v", "--validate" -> arguments[VALIDATE] = VALIDATE
            "-p", "--prettify" -> arguments[PRETTIFY] = PRETTIFY

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
        """Usage: java -jar [path]kmltogpx-<version>-all.jar <KML file> [options]
           |Run KML to GPX, a util that coverts a KML file to a GPX file.
           |
           |Arguments:
           | KML file: Path to the KML file to convert (with file extension)
           | options:
           |  -o, --output   <path to output file> Write the result to the given file. If not specified, the output will be printed to stdout 
           |  -v, --validate                       Validate that the output is according to GPX specification
           |  -p, --prettify                       Prettify the output
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

private fun convert(
    kmlPath: String,
    outputPath: String? = null,
    validate: Boolean = false,
    prettify: Boolean = false,
) {
    println("Converting '$kmlPath' from KML to GPX")
    assertFileExists(kmlPath)
    val kml = FileInputStream(kmlPath)
    convert(kml = kml, outputPath = outputPath, validate = validate, prettify = prettify)
}

fun convert(
    kml: InputStream,
    outputPath: String? = null,
    validate: Boolean = false,
    prettify: Boolean = false,
) {
    val route = parseKml(kml)
    var gpx = route.toGpx()
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
