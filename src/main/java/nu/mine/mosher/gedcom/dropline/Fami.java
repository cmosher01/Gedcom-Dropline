package nu.mine.mosher.gedcom.dropline;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Fami {
    public static final double BAR_HEIGHT = 4;
    public static final double MARRIAGE_SPACING = 20;
    public static final double CHILD_LINE_DISTANCE = 10;
    public static final double CHILD_HEIGHT = 10;
    public static final SvgBuilder.ClassAttr CLS_BAR_PARENT = new SvgBuilder.ClassAttr("barParent");
    public static final SvgBuilder.ClassAttr CLS_BAR_DESCENT = new SvgBuilder.ClassAttr("barDescent");
    public static final SvgBuilder.ClassAttr CLS_BAR_CHILD = new SvgBuilder.ClassAttr("barChild");



    private Indi husb;
    private Indi wife;
    private List<Indi> rChild = new ArrayList<>();

    private Line2D parentBar1;
    private Line2D parentBar2;

    private Line2D descentBar1;
    private Line2D descentBar2;
    private Line2D descentBar3;

    private Line2D childBar;
    private Line2D[] rChildBar;

    public void setHusb(Indi indi) {
        husb = indi;
    }

    public void setWife(Indi indi) {
        wife = indi;
    }

    public void addChild(Indi indi) {
        rChild.add(indi);
    }

    private void calc() {
        if (husb == null && wife == null && rChild.size() == 0) {
            return;
        }

        Rectangle2D rect1;
        Rectangle2D rect2;
        if (husb == null && wife == null) {
            rect1 = new Rectangle2D.Double();
            rect2 = new Rectangle2D.Double();
        } else if (husb == null) {
            rect2 = wife.getBounds();
            rect1 = wife.getBounds();
            rect1.setRect(rect1.getMinX() - MARRIAGE_SPACING, rect1.getY(), 0, rect1.getHeight());
        } else if (wife == null) {
            rect1 = husb.getBounds();
            rect2 = husb.getBounds();
            rect2.setRect(rect2.getMaxX() + MARRIAGE_SPACING, rect2.getY(), 0, rect2.getHeight());
        } else {
            rect1 = husb.getBounds();
            rect2 = wife.getBounds();
        }

        boolean bHusbandOnRight = (rect1.getX() > rect2.getX());
        if (bHusbandOnRight) {
            Rectangle2D temp = rect1;
            rect1 = rect2;
            rect2 = temp;
        }

        Point2D pt1 = new Point2D.Double(rect1.getMaxX(), rect1.getCenterY());
        Point2D pt2 = new Point2D.Double(rect2.getMinX(), rect2.getCenterY());
        if (bHusbandOnRight) {
            Point2D temp = pt1;
            pt1 = pt2;
            pt2 = temp;
        }

        final double dx = pt2.getX() - pt1.getX();
        final double dy = pt2.getY() - pt1.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (-1e-8 < Math.rint(dist) && Math.rint(dist) < 1e-8) {
            dist = 1;
        }

        final double nx = pt1.getX() + (CHILD_LINE_DISTANCE * dx / dist);
        final double ny = pt1.getY() + (CHILD_LINE_DISTANCE * dy / dist) + BAR_HEIGHT / 2;
        final Point2D ptP = new Point2D.Double(nx, ny);



        if (pt1.getX() > 0 || pt1.getY() > 0 || pt2.getX() > 0 || pt2.getY() > 0) {
            parentBar1 = new Line2D.Double(pt1.getX(), pt1.getY() - BAR_HEIGHT / 2, pt2.getX(), pt2.getY() - BAR_HEIGHT / 2);
            parentBar2 = new Line2D.Double(pt1.getX(), pt1.getY() + BAR_HEIGHT / 2, pt2.getX(), pt2.getY() + BAR_HEIGHT / 2);
        }

        double nTop = Double.MAX_VALUE;
        double nBottom = Double.MIN_VALUE;
        double nLeft = Double.MAX_VALUE;
        double nRight = Double.MIN_VALUE;
        if (!rChild.isEmpty()) {
            Point2D[] rp = new Point2D.Double[rChild.size()];
            for (int i = 0; i < rp.length; i++) {
                final Rectangle2D rect = rChild.get(i).getBounds();

                double x = rect.getCenterX();
                double y = rect.getY();
                rp[i] = new Point2D.Double(x, y);

                if (x < nLeft) {
                    nLeft = x;
                }
                if (x > nRight) {
                    nRight = x;
                }

                if (y < nTop) {
                    nTop = y;
                }
                if (y > nBottom) {
                    nBottom = y;
                }
            }
            nTop -= CHILD_HEIGHT;
            childBar = new Line2D.Double(nLeft, nTop, nRight, nTop);

            rChildBar = new Line2D.Double[rp.length];
            for (int i = 0; i < rChildBar.length; i++) {
                rChildBar[i] = new Line2D.Double(rp[i].getX(), rp[i].getY(), rp[i].getX(), nTop);
            }

            if (pt1.getX() > 0 || pt1.getY() > 0 || pt2.getX() > 0 || pt2.getY() > 0) {
                if (nLeft < ptP.getX() && ptP.getX() < nRight) {
                    descentBar1 = new Line2D.Double(ptP.getX(), ptP.getY(), ptP.getX(), nTop);
                } else {
                    descentBar1 = new Line2D.Double(ptP.getX(), ptP.getY(), ptP.getX(), nTop - CHILD_HEIGHT);
                    descentBar2 = new Line2D.Double(ptP.getX(), nTop - CHILD_HEIGHT, (nRight + nLeft) / 2, nTop - CHILD_HEIGHT);
                    descentBar3 = new Line2D.Double((nRight + nLeft) / 2, nTop - CHILD_HEIGHT, (nRight + nLeft) / 2, nTop);
                }
            }
        }
    }

    public void buildInto(final SvgBuilder svg) {
        calc();

        buildLine(svg, this.parentBar1, CLS_BAR_PARENT);
        buildLine(svg, this.parentBar2, CLS_BAR_PARENT);

        buildLine(svg, this.descentBar1, CLS_BAR_DESCENT);
        buildLine(svg, this.descentBar2, CLS_BAR_DESCENT);
        buildLine(svg, this.descentBar3, CLS_BAR_DESCENT);

        buildLine(svg, this.childBar, CLS_BAR_CHILD);
        if (!Objects.isNull(this.rChildBar)) {
            Arrays.stream(this.rChildBar).forEach(c -> buildLine(svg, c, CLS_BAR_CHILD));
        }
    }

    private static void buildLine(final SvgBuilder svg, final Line2D line, final SvgBuilder.ClassAttr cls) {
        if (Objects.isNull(line)) {
            return;
        }
        svg.add(line, Optional.of(cls));
    }
}
