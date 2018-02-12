package nu.mine.mosher.gedcom.dropline;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class FamilyChartPanel extends JPanel {
    private final FamilyChart chart;
    private boolean mInitialized;

    public FamilyChartPanel(final FamilyChart chart) {
        this.chart = chart;
    }

    public void paint(final Graphics g) {
        synchronized (this) {
            if (!mInitialized) {
                final Dimension dimBounds = this.chart.init(g);
                setSize(dimBounds);
                setPreferredSize(dimBounds);
                this.mInitialized = true;
            }
        }
        this.chart.paint(g);
    }
}
