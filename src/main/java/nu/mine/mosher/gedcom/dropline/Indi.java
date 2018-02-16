package nu.mine.mosher.gedcom.dropline;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.awt.font.TextAttribute.FONT;
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
    public static final SvgBuilder.ClassAttr CLS_DEATH = new SvgBuilder.ClassAttr("death");

    private final double x;
    private final double y;
    private final String id;
    private final String name;
    private final String birth;
    private final String death;

    private double w;
    private double h;
    private final List<Line> lines = new ArrayList<>();



    private static class Line {
        final String text;
        final Point2D pt;
        final SvgBuilder.ClassAttr cls;

        public Line(final String text, final Point2D pt, final SvgBuilder.ClassAttr cls) {
            this.text = text;
            this.pt = pt;
            this.cls = cls;
        }

        public void buildInto(SvgBuilder svg) {
            svg.add(this.text, this.pt, Optional.of(this.cls));
        }
    }



    public Indi(double x, double y, String id, String name, String birth, String death) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.name = name;
        this.birth = birth;
        this.death = death;
    }

    public String getId() {
        return this.id;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x, y, w, h);
    }

    private void setMaxWidth(final double w) {
        this.w = max(this.w, w);
    }

    private double getMaxWidth(final Graphics2D g) {
        return MAX_WIDTH_EMS * g.getFontMetrics().stringWidth("M") - LEFT_MARGIN - RIGHT_MARGIN;
    }

    private static AttributedCharacterIterator getAttrStr(final String line, final Graphics2D g) {
        return new AttributedString(line, singletonMap(FONT, g.getFont())).getIterator();
    }



    public void buildInto(final SvgBuilder svg) {
        lines.clear();
        this.w = 0;
        this.h = TOP_MARGIN;

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

        if (!name.isEmpty()) {
            appendToDisplay(g, name, CLS_NAME);
        }
        if (!birth.isEmpty()) {
            appendToDisplay(g, "\u200a*\u200a" + birth, CLS_BIRTH);
        }
        if (!death.isEmpty()) {
            appendToDisplay(g, "\u2020" + death, CLS_DEATH);
        }

        this.h += BOTTOM_MARGIN;

        this.lines.forEach(line -> line.buildInto(svg));
    }

    private Graphics2D buildGraphics() {
        final BufferedImage img = new BufferedImage(DIM, DIM, BufferedImage.TYPE_BYTE_BINARY);
        final Graphics2D g = img.createGraphics();
        return configureGraphics(g);
    }

    private Graphics2D configureGraphics(final Graphics2D g) {
        g.setFont(Font.decode(FONT_LOGICAL_NAME).deriveFont(FONT_POINT_SIZE).deriveFont(getFontAttrs()));

        return g;
    }

    private static Map<TextAttribute, Integer> getFontAttrs() {
        final Map<TextAttribute, Integer> map = new HashMap<>();
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        map.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        return Collections.unmodifiableMap(map);
    }

    private void appendToDisplay(final Graphics2D g, final String str, final SvgBuilder.ClassAttr cls) {
        final double maxWidth = getMaxWidth(g);
        final LineBreakMeasurer breaker = new LineBreakMeasurer(getAttrStr(str, g), g.getFontRenderContext());

        int curPos = 0;
        double drawPosY = this.y + this.h;
        while (breaker.getPosition() < str.length()) {
            final int nextPos = breaker.nextOffset((float)maxWidth);
            final String line = str.substring(curPos, nextPos);
            curPos = nextPos;

            final TextLayout layout = breaker.nextLayout((float)maxWidth);
            double drawPosX = this.x + (layout.isLeftToRight() ? LEFT_MARGIN : maxWidth - RIGHT_MARGIN - layout.getAdvance());
            drawPosY += layout.getAscent();

            this.lines.add(new Line(line, new Point2D.Double(drawPosX, drawPosY), cls));

            drawPosY += layout.getDescent() + layout.getLeading();
            setMaxWidth(LEFT_MARGIN + layout.getAdvance() + RIGHT_MARGIN);
        }
        this.h = drawPosY - this.y;
    }
}
