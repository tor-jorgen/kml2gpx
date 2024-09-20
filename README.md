# Kml2Gpx

Converts from KML 2.2 to GPX 1.1.

This application was made to be able to use paths from Google MyMaps in OsmAnd (or any other application that supports
GPX files).

## KML

https://developers.google.com/kml/documentation/kmlreference

## GPX

https://www.topografix.com/GPX/1/1/

## Translation from KML to GPX

| KML                 | GPX      | Comment                                                               |
|---------------------|----------|-----------------------------------------------------------------------|
| Path (`LineString`) | Track    |                                                                       |   
| Simple `Placemark`  | Waypoint | All the simple plackemarks are gathered in a single list of waypoints |

Other elements are ignored.

## Development

### Dependencies

#### Java

Java 20

### Build application

Execute the following from the command line to build the application:

    # Linux    
    ./gradlew clean build

    # Windows
    gradle clean build

### Run application

Note that a Java Runtime Environment (JRE) must be installed to be able to run this application.

Execute the following from the command line to run the application and show the help screen:

    java -jar [path]kml2gpx-<version>-all.jar

E.g.:

    java -jar build/kml2gpx-1.0.1-all.jar
