package nu.mine.mosher.gedcom.dropline;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;

class IndiSet {
    private final List<Indi> mrIndi = new ArrayList<>();

    public void add(Indi indi) {
        mrIndi.add(indi);
    }

    public Rectangle init(final Graphics g) {
        final Rectangle bounds = new Rectangle();

        for (final Indi indi : mrIndi) {
            bounds.add(indi.calc(g));
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
