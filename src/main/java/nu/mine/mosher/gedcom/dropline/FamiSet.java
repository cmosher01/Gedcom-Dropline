package nu.mine.mosher.gedcom.dropline;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class FamiSet {
    private final List<Fami> mrFami = new ArrayList<>();

    public void add(Fami fami) {
        mrFami.add(fami);
    }

    public void init(Graphics g) {
        mrFami.forEach(f -> f.calc(g));
    }

    public void paint(Graphics g) {
        final Rectangle2D clip = g.getClipBounds();

        mrFami.forEach(f -> {
            if (f.sect(clip)) {
                f.paint(g);
            }
        });
    }
}
