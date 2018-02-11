package nu.mine.mosher.gedcom.dropline;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Fami {
    private static final int BAR_HEIGHT = 4;
    private static final int MARRIAGE_SPACING = 20;
    private static final int CHILD_LINE_DISTANCE = 10;
    private static final int CHILD_HEIGHT = 10;

    private Indi husb;
    private Indi wife;
    private List rChild = new ArrayList(); // <Indi>

    private Line2D parentBar1;
    private Line2D parentBar2;

    private Line2D descentBar1;
    private Line2D descentBar2;
    private Line2D descentBar3;

    private Line2D childBar;
    private Line2D[] rChildBar;

    private Shape parentBounds; //Polygon
    private Shape descentLine; //Line2D
    private Shape childBounds; //Polygon

    public void setHusb(Indi indi) {
        husb = indi;
    }

    public void setWife(Indi indi) {
        wife = indi;
    }

    public void addChild(Indi indi) {
        rChild.add(indi);
    }

    public void calc(Graphics g) {
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

        double dx = pt2.getX() - pt1.getX();
        double dy = pt2.getY() - pt1.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (-1e-8 < Math.rint(dist) && Math.rint(dist) < 1e-8) {
            dist = 1;
        }

        double nx = pt1.getX() + (CHILD_LINE_DISTANCE * dx / dist);
        double ny = pt1.getY() + (CHILD_LINE_DISTANCE * dy / dist) + BAR_HEIGHT / 2;
        Point2D ptP = new Point2D.Double(nx, ny);



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
                Rectangle2D rect = ((Indi) rChild.get(i)).getBounds();

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



        Polygon pg = new Polygon();
        if (parentBar1 != null) {
            pg.addPoint((int) parentBar1.getX1(), (int) parentBar1.getY1());
            pg.addPoint((int) parentBar1.getX2(), (int) parentBar1.getY2());
            pg.addPoint((int) parentBar2.getX2(), (int) parentBar2.getY2());
            pg.addPoint((int) parentBar2.getX1(), (int) parentBar2.getY1());
        }
        parentBounds = pg;

        Line2D ln = new Line2D.Double();
        if (descentBar1 != null) {
            ln.setLine(descentBar1);
        }
        descentLine = ln;

        Rectangle rect = new Rectangle();
        if (descentBar1 != null) {
            double y = descentBar1.getY2();
            double x = Math.min(nLeft, ptP.getX());
            double w = Math.max(nRight, ptP.getX()) - x + 1;
            if (w < 1) {
                w = 1;
            }
            double h = nBottom - y + 1;
            if (h < 1) {
                h = 1;
            }
            rect.setRect(x, y, w, h);
        }
        childBounds = rect;
    }

    public void paint(Graphics g) {
        drawLine(g, parentBar1);
        drawLine(g, parentBar2);
        drawLine(g, descentBar1);
        drawLine(g, descentBar2);
        drawLine(g, descentBar3);
        drawLine(g, childBar);
        if (rChildBar != null) {
            for (int i = 0; i < rChildBar.length; i++) {
                drawLine(g, rChildBar[i]);
            }
        }
    }

    protected void drawLine(Graphics g, Line2D line) {
        if (line == null) {
            return;
        }

        g.drawLine((int) Math.round(line.getX1()), (int) Math.round(line.getY1()), (int) Math.round(line.getX2()), (int) Math.round(line.getY2()));
    }

    public boolean sect(Rectangle2D clip) {
        if (childBounds.intersects(clip)) {
            return true;
        }

        if (parentBounds.intersects(clip)) {
            return true;
        }

        if (descentLine.intersects(clip)) {
            return true;
        }

        return false;
    }
}
