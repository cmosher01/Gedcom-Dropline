package nu.mine.mosher.gedcom.dropline;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.Gedcom;
import nu.mine.mosher.gedcom.GedcomLine;
import nu.mine.mosher.gedcom.GedcomTag;
import nu.mine.mosher.gedcom.GedcomTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;

public class Dropline extends JApplet {
    private static final int MAX_INDI_WIDTH = 200;

    private final BufferedInputStream streamGedcom;
    private FamilyChart fc;

    public Dropline(final BufferedInputStream streamGedcom) throws HeadlessException {
        this.streamGedcom = streamGedcom;
    }

    public void init() {
        try {
            super.init();
            tryinit();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    protected static void useOSLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignoreAnyExceptions) {
        }
    }

    protected void tryinit() throws Exception {
        useOSLookAndFeel();

        final GedcomTree tree = Gedcom.readFile(this.streamGedcom);
        readFrom(tree);

        JScrollPane scr = new JScrollPane(fc, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scr);
    }

    private void readFrom(final GedcomTree tree) {
        final Map<String, Indi> mapIdToIndi = new HashMap<>();

        final IndiSet indis = new IndiSet();
        tree.getRoot().forEach(nodeIndi -> {
            if (nodeIndi.getObject().getTag().equals(GedcomTag.INDI)) {
                final Indi indi = buildIndi(nodeIndi);
                mapIdToIndi.put(indi.getId(), indi);
                indis.add(indi);
            }
        });

        final FamiSet famis = new FamiSet();
        tree.getRoot().forEach(nodeFami -> {
            if (nodeFami.getObject().getTag().equals(GedcomTag.FAM)) {
                final Fami fami = buildFami(nodeFami, Collections.unmodifiableMap(mapIdToIndi));
                famis.add(fami);
            }
        });

        indis.setMaxWidth(MAX_INDI_WIDTH);

        this.fc = new FamilyChart(indis, famis);
    }

    private Indi buildIndi(final TreeNode<GedcomLine> nodeIndi) {
        final int[] xy = toCoord(getChildValue(nodeIndi, "_XY"));
        final GedcomLine lineIndi = nodeIndi.getObject();
        final String name = toName(getChildValue(nodeIndi, "NAME"));
        final String birth = toDate(getChildEventDate(nodeIndi, "BIRT"));
        final String death = toDate(getChildEventDate(nodeIndi, "DEAT"));
        final String id = lineIndi.getID();
        return new Indi(xy[0], xy[1], id, name, birth, death);
    }

    private int[] toCoord(final String xy) {
        if (Objects.isNull(xy) || xy.isEmpty()) {
            return new int[] {0,0};
        }
        int[] r = Arrays.stream(xy.split("\\s+")).mapToInt(Dropline::parseInt).toArray();
        if (r.length != 2) {
            System.err.println("Could not parse _XY: "+xy);
            return new int[] {0,0};
        }
        return r;
    }

    private static int parseInt(final String s) {
        if (Objects.isNull(s)  || s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (final Throwable ignore) {
            System.err.println("Could not parse value from _XY tag: "+s);
            return 0;
        }
    }

    private String toName(final String name) {
        return name.replaceAll("/", "");
    }

    private String toDate(final String date) {
        return date;
    }

    private String getChildEventDate(final TreeNode<GedcomLine> node, final String tag) {
        for (final TreeNode<GedcomLine> c : node) {
            if (c.getObject().getTagString().equals(tag)) {
                return getChildValue(c, "DATE");
            }
        }
        return "";
    }

    private String getChildValue(final TreeNode<GedcomLine> node, final String tag) {
        for (final TreeNode<GedcomLine> c : node) {
            if (c.getObject().getTagString().equals(tag)) {
                return c.getObject().getValue();
            }
        }
        return "";
    }

    private Fami buildFami(final TreeNode<GedcomLine> nodeFami, final Map<String, Indi> mapIdToIndi) {
        final Fami fami = new Fami();
        for (final TreeNode<GedcomLine> c : nodeFami) {
            final GedcomLine child = c.getObject();
            switch (child.getTag()) {
                case HUSB: fami.setHusb(mapIdToIndi.get(child.getPointer())); break;
                case WIFE: fami.setWife(mapIdToIndi.get(child.getPointer())); break;
                case CHIL: fami.addChild(mapIdToIndi.get(child.getPointer())); break;
            }
        }
        return fami;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java Dropline input.ged");
        }
        final File infile = new File(args[0]);
        final Frame f = new Frame(infile.getCanonicalPath());
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        final Dropline applet = new Dropline(new BufferedInputStream(new FileInputStream(infile)));
        applet.init();
        f.add(applet);

        f.setSize(1280, 960);
        f.setVisible(true);
    }
}
