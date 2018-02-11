package nu.mine.mosher.gedcom.dropline;

import java.awt.*;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class IndiSet {
    private final List mrIndi = new ArrayList(); // <Indi>

    private int mMaxWidth;



    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    public void add(Indi indi) {
        mrIndi.add(indi);
    }

    public Rectangle init(Graphics g) {
        Rectangle bounds = new Rectangle();

        for (Iterator i = mrIndi.iterator(); i.hasNext(); ) {
            Indi indi = (Indi) i.next();

            Rectangle boundsIndi = indi.calc(g, mMaxWidth);
            bounds.add(boundsIndi);
        }

        return bounds;
    }

    public void paint(Graphics g) {
        RectangularShape clip = g.getClipBounds();

        for (Iterator i = mrIndi.iterator(); i.hasNext(); ) {
            Indi indi = (Indi) i.next();

            if (indi.sect(clip)) {
                indi.paint(g);
            }
        }
    }

    public Indi isOnIndi(Point point) {
        for (Iterator i = mrIndi.iterator(); i.hasNext(); ) {
            Indi indi = (Indi) i.next();

            if (indi.isOn(point)) {
                return indi;
            }
        }

        return null;
    }
}
