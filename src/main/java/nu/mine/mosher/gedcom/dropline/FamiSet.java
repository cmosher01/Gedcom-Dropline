package to.go.gro.groapplet;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FamiSet
{
    private final List mrFami = new ArrayList(); // <Indi>

    public void add(Fami fami)
    {
        mrFami.add(fami);
    }

    public void init(Graphics g)
    {
        for (Iterator i = mrFami.iterator(); i.hasNext();)
        {
            Fami fami = (Fami)i.next();
            fami.calc(g);
        }
    }

    public void paint(Graphics g)
    {
        Rectangle2D clip = g.getClipBounds();

        for (Iterator i = mrFami.iterator(); i.hasNext();)
        {
            Fami fami = (Fami)i.next();

            if (fami.sect(clip))
                fami.paint(g);
        }
    }
}
