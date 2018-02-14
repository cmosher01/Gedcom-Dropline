package nu.mine.mosher.gedcom.dropline;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.awt.font.TextAttribute.FONT;
import static java.lang.Math.*;
import static java.util.Collections.*;

class Indi {
    private static final int MAX_WIDTH_EMS = 16;

    private static final int TOP_MARGIN = 1;
    private static final int RIGHT_MARGIN = 3;
    private static final int BOTTOM_MARGIN = 3;
    private static final int LEFT_MARGIN = 3;

    private final double x;
    private final double y;
    private final String id;
    private final String name;
    private final String birth;
    private final String death;

    private double w;
    private double h;
    private final List<Line> lines = new ArrayList<>();



    public String getId() {
        return this.id;
    }

    public Indi(int x, int y, String id, String name, String birth, String death) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.name = name;
        this.birth = birth;
        this.death = death;
    }

    public Rectangle calc(Graphics g) {
        lines.clear();
        this.w = 0;
        this.h = TOP_MARGIN;

        if (!name.isEmpty()) {
            appendToDisplay(g, name);
        }
        if (!birth.isEmpty()) {
            appendToDisplay(g, "*"+birth);
        }
        if (!death.isEmpty()) {
            appendToDisplay(g, "\u2020"+death);
        }

        this.h += BOTTOM_MARGIN;

        return getBoundsInt();
    }

    private Rectangle getBoundsInt() {
        return new Rectangle((int)floor(x), (int)floor(y), (int) ceil(w), (int) ceil(h));
    }


    public boolean sect(final RectangularShape rect) {
        if (Objects.isNull(rect)) {
            return true;
        }
        return rect.intersects(x, y, w, h);
    }

    public void paint(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        ((Graphics2D) g).draw(getBounds());

        g.setColor(Color.BLACK);
        this.lines.forEach(line -> line.paint(g));
    }

    public String toString() {
        return "Indi: " + name + " [" + x + "," + y + "," + w + "," + h + "]";
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, w, h);
    }


    private static class Line {
        final String text;
        final Point2D.Double pt;

        public Line(final String text, final Point2D.Double pt) {
            this.text = text;
            this.pt = pt;
        }

        public void paint(Graphics g) {
            g.drawString(this.text, (int)rint(this.pt.x), (int)rint(this.pt.y));
        }
    }

    private void appendToDisplay(final Graphics gr, final String str) {
        final Graphics2D g = (Graphics2D) gr;

        final float maxWidth = getMaxWidth(g);
        final LineBreakMeasurer breaker = new LineBreakMeasurer(getAttrStr(str, g), g.getFontRenderContext());

        int curPos = 0;
        double drawPosY = this.y + this.h;
        while (breaker.getPosition() < str.length()) {
            final int nextPos = breaker.nextOffset(maxWidth);
            final String line = str.substring(curPos, nextPos);
            curPos = nextPos;

            final TextLayout layout = breaker.nextLayout(maxWidth);
            double drawPosX = this.x + (layout.isLeftToRight() ? LEFT_MARGIN : maxWidth-RIGHT_MARGIN-layout.getAdvance());
            drawPosY += layout.getAscent();

            this.lines.add(new Line(line, new Point2D.Double(drawPosX, drawPosY)));

            drawPosY += layout.getDescent() + layout.getLeading();
            setActualWidth(LEFT_MARGIN + layout.getAdvance() + RIGHT_MARGIN);
        }
        this.h = drawPosY - this.y;
    }

    private void setActualWidth(final double w) {
        this.w = max(this.w, w);
    }

    private float getMaxWidth(final Graphics2D g) {
        return MAX_WIDTH_EMS * g.getFontMetrics().stringWidth("M") - LEFT_MARGIN - RIGHT_MARGIN;
    }

    private static AttributedCharacterIterator getAttrStr(final String line, final Graphics2D g) {
        return new AttributedString(line, singletonMap(FONT, g.getFont())).getIterator();
    }
}
