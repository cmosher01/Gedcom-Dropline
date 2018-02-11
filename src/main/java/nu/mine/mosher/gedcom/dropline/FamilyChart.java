package nu.mine.mosher.gedcom.dropline;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

public class FamilyChart extends JPanel {
    private final Applet mApplet;
    private final IndiSet mIndis;
    private final FamiSet mFamis;
    private boolean mInitialized;

    public FamilyChart(Applet applet, IndiSet indis, FamiSet famis) {
        mApplet = applet;
        mIndis = indis;
        mFamis = famis;
        GROMouseListener ml = new GROMouseListener(this);
        addMouseListener(ml);
        addMouseMotionListener(ml);
    }

    public void paint(Graphics g) {
        if (!mInitialized) {
            init(g);
        }

        Rectangle clip = g.getClipBounds();
        g.clearRect(clip.x, clip.y, clip.width, clip.height);

        mFamis.paint(g);
        mIndis.paint(g);
    }

    protected void init(Graphics g) {
        Rectangle bounds = mIndis.init(g);
        mFamis.init(g);

        Dimension dimBounds = new Dimension(bounds.width, bounds.height);
        setSize(dimBounds);
        setPreferredSize(dimBounds);

        mInitialized = true;
    }

    public Indi hitIndi(Point point) {
        return mIndis.isOnIndi(point);
    }

    public void gotoIndi(Indi mLastIndi) {
        String relurl = mLastIndi.getRelativeURL();
        URL url = null;
        try {
            url = new URL(mApplet.getDocumentBase(), relurl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mApplet.getAppletContext().showDocument(url);
    }
}
