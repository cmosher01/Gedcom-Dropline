# Gedcom-Dropline

Copyright © 2018, Christopher Alan Mosher, Shelton, Connecticut, USA, <cmosher01@gmail.com>.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)
[![License](https://img.shields.io/github/license/cmosher01/Gedcom-Dropline.svg)](https://www.gnu.org/licenses/gpl.html)

Java library to generate a drop-line genealogy chart in SVG.

TODO: Update this library with the improved layout algorithm from
[GEDCOM XY Editor](https://github.com/cmosher01/Gedcom-XY-Editor)

Reads in a GEDCOM file containing special custom _XY tags on
each individual. The _XY tags contain the (x,y) coordinates
of each individual on a drop-line chart.

Generates an SVG file of the inidividuals laid out on a
drop-line style chart.

Can also be used as a library from java programs:

```gradle
repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile group: 'nu.mine.mosher.gedcom.dropline', name: 'gedcom-dropline', version: 'latest.integration'
}
```
