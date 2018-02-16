# Gedcom-Dropline

Java library to generate a drop-line genealogy chart in SVG.

Reads in a GEDCOM file containing special custom _XY tags on
each individual. The _XY tags contain the (x,y) coordinates
of each individual on a drop-line chart.

Generates an SVG file of the inidividuals laid out on a
drop-line style chart.

Can also be used as a library from java programs:

```gradle
repositories {
    mavenCentral()
    maven {
        url 'http://mosher.mine.nu/nexus/repository/maven-public/'
    }
}

dependencies {
    compile group: 'nu.mine.mosher.gedcom.dropline', name: 'gedcom-dropline', version: 'latest.integration'
}
```
