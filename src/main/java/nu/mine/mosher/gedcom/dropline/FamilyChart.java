package nu.mine.mosher.gedcom.dropline;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class FamilyChart extends JPanel {
    public static final String FONT_LOGICAL_NAME = "Trebuchet MS";
//    public static final String FONT_LOGICAL_NAME = "Georgia";
    private final IndiSet mIndis;
    private final FamiSet mFamis;
    private boolean mInitialized;

    public FamilyChart(IndiSet indis, FamiSet famis) {
        mIndis = indis;
        mFamis = famis;
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
        Font f = Font.decode(FONT_LOGICAL_NAME);
        f = f.deriveFont(9.0F);
        Map<TextAttribute, Object> map = new HashMap<>();
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        map.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        f = f.deriveFont(map);
        g.setFont(f);

        Rectangle bounds = mIndis.init(g);
        mFamis.init(g);

        Dimension dimBounds = new Dimension(bounds.width, bounds.height);
        setSize(dimBounds);
        setPreferredSize(dimBounds);

        mInitialized = true;
    }
}
