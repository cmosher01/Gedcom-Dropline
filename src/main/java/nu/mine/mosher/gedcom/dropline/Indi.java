package nu.mine.mosher.gedcom.dropline;

import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

class Indi {
    private static final int LEFT_MARGIN = 3;
    private static final int RIGHT_MARGIN = 4;

    private final int x;
    private final int y;
    private final String mID;
    private final String mName;
    private final String mBirth;
    private final String mDeath;

    private int w;
    private int h;
    private final List<TextLine> mLines = new ArrayList<>();



    public String getId() {
        return this.mID;
    }

    private class TextLine {
        private TextLayout text;
        private float y;

        TextLine(TextLayout text, float y) {
            this.text = text;
            this.y = y;
        }

        void draw(Graphics g, int x) {
            text.draw((Graphics2D) g, x, y);
        }
    }

    public Indi(int x, int y, String id, String name, String birth, String death) {
        this.x = x;
        this.y = y;
        mID = id;
        mName = name;
        mBirth = birth;
        mDeath = death;
    }

    public Rectangle calc(Graphics g, int maxWidth) {
        mLines.clear();

        if (mName.length() > 0) {
            calcBreaks(g, mName, maxWidth);
        }
        if (mBirth.length() > 0) {
            calcBreaks(g, mBirth, maxWidth);
        }
        if (mDeath.length() > 0) {
            calcBreaks(g, mDeath, maxWidth);
        }

        return new Rectangle(x, y, w, h);
    }

    protected void calcBreaks(final Graphics gr, final String s, final int maxWidth) {
        final Graphics2D g = (Graphics2D) gr;
        final AttributedString attr = new AttributedString(s);
        attr.addAttribute(TextAttribute.FONT, g.getFont());
        final LineBreakMeasurer breaker = new LineBreakMeasurer(attr.getIterator(), g.getFontRenderContext());

        float cy = y + h;
        while (breaker.getPosition() < s.length()) {
            final TextLayout text = breaker.nextLayout(maxWidth);
            cy += text.getAscent();
            w = (int) Math.rint(Math.max((float) w, text.getAdvance() + RIGHT_MARGIN + 1));
            mLines.add(new TextLine(text, cy));
            cy += text.getDescent() + text.getLeading();
        }
        h = (int) Math.rint(cy) - y;
    }

    public boolean sect(RectangularShape rect) {
        return rect.intersects(x, y, w, h);
    }

    public void paint(Graphics g) {
        for (TextLine text : mLines) {
            text.draw(g, x + LEFT_MARGIN);
        }
    }

    public String toString() {
        return "Indi: " + mName + " [" + x + "," + y + "," + w + "," + h + "]";
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x, y, w, h);
    }
}
