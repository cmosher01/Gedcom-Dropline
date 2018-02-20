package nu.mine.mosher.gedcom.dropline;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SvgBuilder {
    private static final String W3C_SVG_NS_URI = "http://www.w3.org/2000/svg";

    private final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    private final Element svg = this.doc.createElementNS(W3C_SVG_NS_URI, "svg");

    public static class ClassAttr {
        private final String name;

        public ClassAttr(String name) {
            this.name = name;
        }

        private void set(Element e) {
            e.setAttribute("class", this.name);
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public SvgBuilder() throws ParserConfigurationException {
        this.doc.appendChild(svg);
    }

    public void add(final Line2D line, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") final Optional<ClassAttr> cls) {
        final Element e = this.doc.createElement("line");
        addPointTo(line.getP1(), e, "1");
        addPointTo(line.getP2(), e, "2");
        cls.ifPresent(c -> c.set(e));
        this.svg.appendChild(e);
    }

    public void add(final String string, final Point2D at, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") final Optional<ClassAttr> cls, final String dataAttr, final String dataVal) {
        final Element e = this.doc.createElement("text");
        addPointTo(at, e, "");
        cls.ifPresent(c -> c.set(e));
        if (!dataAttr.isEmpty()) {
            e.setAttribute("data-"+dataAttr, dataVal);
        }
        e.appendChild(this.doc.createTextNode(string));
        this.svg.appendChild(e);
    }

    private static void addPointTo(final Point2D pt, final Element e, final String sfx) {
        e.setAttribute("x" + sfx, Double.toString(pt.getX()));
        e.setAttribute("y" + sfx, Double.toString(pt.getY()));
    }

    public Document get() {
        return this.doc;
    }
}
