package nu.mine.mosher.gedcom.dropline;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.*;

import static java.awt.font.TextAttribute.*;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static java.lang.Math.max;
import static java.util.Collections.singletonMap;

class Indi {
    public static final String FONT_LOGICAL_NAME = "Garamond";
    public static final float FONT_POINT_SIZE = 9.0F;
    public static final int MAX_WIDTH_EMS = 16;
    public static final double TOP_MARGIN = 1.0;
    public static final double RIGHT_MARGIN = 3.0;
    public static final double BOTTOM_MARGIN = 3.0;
    public static final double LEFT_MARGIN = 3.0;
    public static final int DIM = 0x800;
    public static final SvgBuilder.ClassAttr CLS_NAME = new SvgBuilder.ClassAttr("fullName");
    public static final SvgBuilder.ClassAttr CLS_BIRTH = new SvgBuilder.ClassAttr("birth");
    public static final String PFX_BIRTH = "\u200a*\u200a";
    public static final SvgBuilder.ClassAttr CLS_DEATH = new SvgBuilder.ClassAttr("death");
    public static final String PFX_DEATH = "\u2020";

    private final String id;
    private final Point2D coords = new Point2D.Double(0, 0);
    private final Dim2D dim = new Dim2D(0, TOP_MARGIN);
    private final List<Line> lines = new ArrayList<>();





    private static class Line {
        private final String text;
        private final Dim2D off;
        private final SvgBuilder.ClassAttr cls;

        private Line(final String text, final Dim2D off, final SvgBuilder.ClassAttr cls) {
            this.text = text;
            this.off = off;
            this.cls = cls;
        }

        private void buildAtInto(Point2D at, SvgBuilder svg) {
            svg.add(this.text, getPoint(at), Optional.of(this.cls));
        }

        private Point2D getPoint(Point2D at) {
            return new Point2D.Double(at.getX() + this.off.getWidth(), at.getY() + this.off.getHeight());
        }
    }





    public Indi(final Point2D coords, String id, String name, String birth, String death) {
        this.id = id;
        this.coords.setLocation(coords);

        /*
        We need to layout the lines of text (3 lines, but will be more
        if we need to wrap an overly long name), and also
        calculate the total width and height, based on the size of
        the drawn text. This will be highly dependent on the font,
        size, kerning, etc., etc. Even so, it may not exactly match
        the bounds on the final rendering display device.
        Do the best we can. Use Garamond font (reasonably pretty,
        free, and ubiquitous), 9 point, normal, with kerning and
        ligatures.
         */
        final Graphics2D g = buildGraphics();
        lines.clear();

        if (!name.isEmpty()) {
            appendToDisplay(g, name, CLS_NAME, this.dim, this.lines);
        }
        if (!birth.isEmpty()) {
            appendToDisplay(g, PFX_BIRTH + birth, CLS_BIRTH, this.dim, this.lines);
        }
        if (!death.isEmpty()) {
            appendToDisplay(g, PFX_DEATH + death, CLS_DEATH, this.dim, this.lines);
        }

        this.dim.setSize(this.dim.getWidth(), this.dim.getHeight()+BOTTOM_MARGIN);
    }

    public String getId() {
        return this.id;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(this.coords.getX(), this.coords.getY(), this.dim.getWidth(), this.dim.getHeight());
    }

    public void buildInto(final SvgBuilder svg) {
        this.lines.forEach(line -> line.buildAtInto(this.coords, svg));
    }

    public void move(final Dim2D delta) {
        this.coords.setLocation(this.coords.getX() + delta.getWidth(), this.coords.getY() + delta.getHeight());
    }





    private static Graphics2D buildGraphics() {
        return configureGraphics(new BufferedImage(DIM, DIM, TYPE_BYTE_BINARY).createGraphics());
    }

    private static Graphics2D configureGraphics(final Graphics2D g) {
        g.setFont(Font.decode(FONT_LOGICAL_NAME).deriveFont(FONT_POINT_SIZE).deriveFont(getFontAttrs()));
        return g;
    }

    private static Map<TextAttribute, Integer> getFontAttrs() {
        final Map<TextAttribute, Integer> map = new HashMap<>();
        map.put(KERNING, KERNING_ON);
        map.put(LIGATURES, LIGATURES_ON);
        return Collections.unmodifiableMap(map);
    }

    private static void appendToDisplay(final Graphics2D g, final String str, final SvgBuilder.ClassAttr cls, final Dim2D dim, final List<Line> lines) {
        final double maxWidth = getMaxWidth(g);
        final LineBreakMeasurer breaker = new LineBreakMeasurer(getAttrStr(str, g), g.getFontRenderContext());

        int cur = 0;
        double dy = dim.getHeight();
        double maxw = dim.getWidth();
        while (breaker.getPosition() < str.length()) {
            final int next = breaker.nextOffset((float) maxWidth);
            final String line = str.substring(cur, next);
            cur = next;

            final TextLayout layout = breaker.nextLayout((float) maxWidth);
            final double dx = layout.isLeftToRight() ? LEFT_MARGIN : -RIGHT_MARGIN + -layout.getAdvance() + maxWidth;
            dy += layout.getAscent();

            lines.add(new Line(line, new Dim2D(dx, dy), cls));

            dy += layout.getDescent() + layout.getLeading();

            maxw = max(maxw, LEFT_MARGIN + layout.getAdvance() + RIGHT_MARGIN);
        }
        dim.setSize(maxw, dy);
    }

    private static double getMaxWidth(final Graphics2D g) {
        return MAX_WIDTH_EMS * g.getFontMetrics().stringWidth("M") - LEFT_MARGIN - RIGHT_MARGIN;
    }

    private static AttributedCharacterIterator getAttrStr(final String line, final Graphics2D g) {
        return new AttributedString(line, singletonMap(FONT, g.getFont())).getIterator();
    }
}
