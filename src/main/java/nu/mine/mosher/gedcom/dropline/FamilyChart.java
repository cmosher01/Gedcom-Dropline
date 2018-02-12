package nu.mine.mosher.gedcom.dropline;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FamilyChart {
//    public static final String FONT_LOGICAL_NAME = "SansSerif";
    public static final String FONT_LOGICAL_NAME = "Trebuchet MS";
//    public static final String FONT_LOGICAL_NAME = "Georgia";

    private final IndiSet mIndis;
    private final FamiSet mFamis;

    public FamilyChart(final IndiSet indis, final FamiSet famis) {
        mIndis = indis;
        mFamis = famis;
    }

    public Rectangle getQuickBounds() {
        return this.mIndis.getQuickBounds();
    }

    public Dimension init(final Graphics g) {
        g.setFont(
            Font.decode(FONT_LOGICAL_NAME)
                .deriveFont(9.0F)
                .deriveFont(getFontAttrs()));
        System.err.println("Using font: "+g.getFont().getName());

        final Rectangle bounds = mIndis.init(g);
        mFamis.init(g);

        return new Dimension(bounds.width, bounds.height);
    }

    public void paint(final Graphics g) {
//        final Rectangle clip = g.getClipBounds();
//        g.clearRect(clip.x, clip.y, clip.width, clip.height);

        mFamis.paint(g);
        mIndis.paint(g);
    }

    private static Map<TextAttribute, Object> getFontAttrs() {
        final Map<TextAttribute, Object> map = new HashMap<>();
//        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
//        map.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        return Collections.unmodifiableMap(map);
    }
}
