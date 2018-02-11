package nu.mine.mosher.gedcom.dropline;

import java.awt.*;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Iterator;
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

    public Rectangle init(Graphics g) {
        Rectangle bounds = new Rectangle();

        for (Indi indi : mrIndi) {
            bounds.add(indi.calc(g, mMaxWidth));
        }

        return bounds;
    }

    public void paint(Graphics g) {
        RectangularShape clip = g.getClipBounds();

        for (Indi indi : mrIndi) {
            if (indi.sect(clip)) {
                indi.paint(g);
            }
        }
    }

    public Indi isOnIndi(Point point) {
        for (Indi indi : mrIndi) {
            if (indi.isOn(point)) {
                return indi;
            }
        }

        return null;
    }
}
