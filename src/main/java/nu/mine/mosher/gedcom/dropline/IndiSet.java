package nu.mine.mosher.gedcom.dropline;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;

class IndiSet {
    private final List<Indi> mrIndi = new ArrayList<>();

    private int mMaxWidth;



    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    public void add(Indi indi) {
        mrIndi.add(indi);
    }

    public Rectangle getQuickBounds() {
        int xMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
        for (final Indi indi : mrIndi) {
            final Point xy = indi.getXY();
            if (xy.x < xMin) {
                xMin = xy.x;
            }
            if (xMax < xy.x) {
                xMax = xy.x;
            }
            if (xy.y < yMin) {
                yMin = xy.y;
            }
            if (yMax < xy.y) {
                yMax = xy.y;
            }
        }
        final Rectangle r = new Rectangle(xMin, yMin, xMax-xMin, yMax-yMin);
        r.grow(this.mMaxWidth, this.mMaxWidth); // not perfect, but a good guess
        return r;
    }

    public Rectangle init(final Graphics g) {
        final Rectangle bounds = new Rectangle();

        for (final Indi indi : mrIndi) {
            bounds.add(indi.calc(g, mMaxWidth));
        }

        return bounds;
    }

    public void paint(final Graphics g) {
        final RectangularShape clip = g.getClipBounds();

        for (final Indi indi : mrIndi) {
            if (indi.sect(clip)) {
                indi.paint(g);
            }
        }
    }
}
