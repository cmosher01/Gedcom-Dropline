package nu.mine.mosher.gedcom.dropline;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Dropline {
    public static void main(String[] args) throws IOException, InvalidLevel, TransformerException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java Dropline input.ged");
        }
        final File infile = new File(args[0]);
        final FamilyChart chart = FamilyChartBuilder.create(new BufferedInputStream(new FileInputStream(infile)));

        final DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        final Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);

        final SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        chart.init(svgGenerator);
        chart.paint(svgGenerator);

        svgGenerator.getRoot(document.getDocumentElement());
        final Node svg = document.getFirstChild();
        final Node g = svg.getChildNodes().item(2);
        ((Element) g).setAttribute("id", "top_g");

        System.out.println("<!doctype html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<style type=\"text/css\">\n" +
            "html, body, svg {\n" +
            "    width: 95%;\n" +
            "    height: 95%;\n" +
            "    margin: 0 auto;\n" +
            "}\n" +
            "</style>\n" +
            "<script src=\"https://cdn.rawgit.com/anvaka/panzoom/v4.1.0/dist/panzoom.js\">\n" +
            "</script>\n" +
            "<script>\n" +
            "    window.onload = function() {\n" +
            "        const g = document.getElementById('top_g');\n" +
            "        panzoom(g, {\n" +
            "            minZoom: 0.025,\n" +
            "            maxZoom: 250,\n" +
            "            zoomSpeed: 0.075,\n" +
            "            smoothScroll: false\n" +
            "        });\n" +
            "    };\n" +
            "</script>\n" +
            "</head>\n" +
            "<body>");
        dumpDocument(document);
        System.out.println("</body>\n</html>");
    }

    private static void dumpDocument(Document doc) throws TransformerException {
        final DOMSource domSource = new DOMSource(doc);
        final StreamResult result = new StreamResult(System.out);
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(domSource, result);
    }
}
