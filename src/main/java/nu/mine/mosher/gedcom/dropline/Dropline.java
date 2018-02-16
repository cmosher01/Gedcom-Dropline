package nu.mine.mosher.gedcom.dropline;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nu.mine.mosher.gedcom.Gedcom;
import nu.mine.mosher.gedcom.GedcomTree;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Dropline {
    public static Document build(final GedcomTree tree) throws IOException, InvalidLevel, ParserConfigurationException {
        final FamilyChart chart = FamilyChartBuilder.create(tree);
        final SvgBuilder svg = new SvgBuilder();
        chart.buildInto(svg);
        return addCss(svg.get());
    }

    private static Document addCss(final Document svg) {
        final Element css = svg.createElementNS("http://www.w3.org/1999/xhtml", "link");
        css.setAttribute("rel", "stylesheet");
        css.setAttribute("type", "text/css");
        css.setAttribute("href", "dropline.css");

        final Element root = svg.getDocumentElement();
        root.insertBefore(css, root.getFirstChild());

        return svg;
    }



    public static void main(String[] args) throws IOException, InvalidLevel, TransformerException, ParserConfigurationException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java Dropline input.ged");
        }
        final Path pathInput = Paths.get(args[0]).toRealPath();
        printDoc(build(Gedcom.readFile(new BufferedInputStream(Files.newInputStream(pathInput)))));
    }

    private static void printDoc(final Document document) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(new DOMSource(document), new StreamResult(System.out));
    }
}
