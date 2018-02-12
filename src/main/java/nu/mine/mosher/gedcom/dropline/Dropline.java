package nu.mine.mosher.gedcom.dropline;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class Dropline extends JApplet {
    private final BufferedInputStream streamGedcom;

    public Dropline(final BufferedInputStream streamGedcom) {
        this.streamGedcom = streamGedcom;
    }

    public void init() {
        try {
            super.init();
            tryinit();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private void tryinit() throws IOException, InvalidLevel {
        useOSLookAndFeel();

        final FamilyChartPanel fc = new FamilyChartPanel(FamilyChartBuilder.create(this.streamGedcom));

        JScrollPane scr = new JScrollPane(fc, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scr);
    }

    private static void useOSLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignoreAnyExceptions) {
        }
    }

    public static void mainSwing(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java Dropline input.ged");
        }
        final File infile = new File(args[0]);
        final Frame f = new Frame(infile.getCanonicalPath());
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        final Dropline applet = new Dropline(new BufferedInputStream(new FileInputStream(infile)));
        applet.init();
        f.add(applet);

        f.setSize(1280, 960);
        f.setVisible(true);
    }

    public static void main(String[] args) throws IOException, InvalidLevel {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java Dropline input.ged");
        }
        final File infile = new File(args[0]);
        final FamilyChart chart = FamilyChartBuilder.create(new BufferedInputStream(new FileInputStream(infile)));
//        final Rectangle bounds = chart.getQuickBounds();




        final DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        final String svgNS = "http://www.w3.org/2000/svg";
        final Document document = domImpl.createDocument(svgNS, "svg", null);

        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
        ctx.setEmbeddedFontsOn(false);
        final SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, false);

        chart.init(svgGenerator);
        chart.paint(svgGenerator);

        final boolean useCSS = true;
        final Writer out = new OutputStreamWriter(System.out, "UTF-8");
        svgGenerator.stream(out, useCSS);
    }
}
