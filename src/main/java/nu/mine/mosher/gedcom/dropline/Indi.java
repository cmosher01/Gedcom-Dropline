package to.go.gro.groapplet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Indi
{
    private static final Color bg = Color.WHITE;
	private static final Color fg = Color.BLACK;
	private static final Color bgpush = Color.LIGHT_GRAY;
	private static final Color fgpush = Color.BLACK;
    private static final int LEFT_MARGIN = 3;
    private static final int RIGHT_MARGIN = 4;

    private final int x;
    private final int y;
	private final int mID;
    private final String mName;
    private final String mBirth;
    private final String mDeath;

    private int w;
    private int h;
    private final List mLines = new ArrayList(); // <TextLine>

	private boolean pushed;

    private class TextLine
    {
        private TextLayout text;
        private float y;
        TextLine(TextLayout text, float y)
        {
            this.text = text;
            this.y = y;
        }
        void draw(Graphics g, int x)
        {
            text.draw((Graphics2D)g,x,y);
        }
    }

    public Indi(int x, int y, int id, String name, String birth, String death)
    {
        this.x = x;
        this.y = y;
        mID = id;
        mName = name;
        mBirth = birth;
        mDeath = death;
    }

    public Rectangle calc(Graphics g, int maxWidth)
    {
        mLines.clear();

        if (mName.length() > 0)
            calcBreaks(g,mName,maxWidth);
        if (mBirth.length() > 0)
            calcBreaks(g,mBirth,maxWidth);
        if (mDeath.length() > 0)
            calcBreaks(g,mDeath,maxWidth);

        return new Rectangle(x,y,w,h);
    }

    protected void calcBreaks(Graphics gr, String s, int maxWidth)
    {
        Graphics2D g = (Graphics2D)gr;
        AttributedString attr = new AttributedString(s);
        LineBreakMeasurer linebreaker = new LineBreakMeasurer(attr.getIterator(),g.getFontRenderContext());

        float cy = y+h;
        while (linebreaker.getPosition() < s.length())
        {
            TextLayout text = linebreaker.nextLayout(maxWidth);
            cy += text.getAscent();
            w = (int)Math.rint(Math.max((float)w,text.getAdvance()+RIGHT_MARGIN+1));
            mLines.add(new TextLine(text,cy));
            cy += text.getDescent() + text.getLeading();
        }
        h = (int)Math.rint(cy)-y;
    }

    public boolean sect(RectangularShape rect)
    {
        return rect.intersects(x,y,w,h);
    }

    public void paint(Graphics g)
    {
        drawBounds(g);
        drawText(g);
    }

    protected void drawBounds(Graphics g)
    {
    	if (pushed)
			g.setColor(bgpush);
		else
	        g.setColor(bg);
        g.fillRect(x,y,w,h);
		if (pushed)
			g.setColor(fgpush);
		else
			g.setColor(fg);
        g.drawRect(x,y,w-1,h-1);
    }

    protected void drawText(Graphics g)
    {
        for (Iterator i = mLines.iterator(); i.hasNext();)
        {
            TextLine text = (TextLine)i.next();
            text.draw(g,x+LEFT_MARGIN);
        }
    }

    public String toString()
    {
        StringBuffer s = new StringBuffer(256);
        s.append("Indi: ");
        s.append(mName);
        s.append(" [");
        s.append(x);
        s.append(",");
        s.append(y);
        s.append(",");
        s.append(w);
        s.append(",");
        s.append(h);
        s.append("]");
        return s.toString();
    }

    public Rectangle2D getBounds()
    {
        return new Rectangle2D.Double(x,y,w,h);
    }

    public boolean isOn(Point point)
    {
        return
            x <= point.x && point.x <= x+w &&
            y <= point.y && point.y <= y+h;
    }

    public void hit(boolean isHit)
    {
    	pushed = isHit;
    }

	public boolean isHit()
	{
		return pushed;
	}

    public String getRelativeURL()
    {
    	return "?indi="+mID;
    }
}
