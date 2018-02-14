package nu.mine.mosher.gedcom.dropline;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.Gedcom;
import nu.mine.mosher.gedcom.GedcomLine;
import nu.mine.mosher.gedcom.GedcomTag;
import nu.mine.mosher.gedcom.GedcomTree;
import nu.mine.mosher.gedcom.exception.InvalidLevel;

public final class FamilyChartBuilder {
    private FamilyChartBuilder() {
        throw new IllegalStateException("not intended to be instantiated");
    }

    public static FamilyChart create(final BufferedInputStream streamGedcom) throws IOException, InvalidLevel {
        final GedcomTree tree = Gedcom.readFile(streamGedcom);

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

        return new FamilyChart(indis, famis);
    }

    private static Indi buildIndi(final TreeNode<GedcomLine> nodeIndi) {
        final String xyval = getChildValue(nodeIndi, "_XY");
        final int[] xy = toCoord(xyval);
        final GedcomLine lineIndi = nodeIndi.getObject();
        final String name = toName(getChildValue(nodeIndi, "NAME"));
        final String birth = toDate(getChildEventDate(nodeIndi, "BIRT"));
        final String death = toDate(getChildEventDate(nodeIndi, "DEAT"));
        final String id = lineIndi.getID();

        if (xyval.isEmpty()) {
            System.err.println("WARNING: missing _XY for: "+name);
        }

        return new Indi(xy[0], xy[1], id, name, birth, death);
    }

    private static int[] toCoord(final String xy) {
        if (Objects.isNull(xy) || xy.isEmpty()) {
            return new int[]{ 0, 0 };
        }
        int[] r = Arrays.stream(xy.split("\\s+")).mapToInt(FamilyChartBuilder::parseInt).toArray();
        if (r.length != 2) {
            System.err.println("Could not parse _XY: " + xy);
            return new int[]{ 0, 0 };
        }
        return r;
    }

    private static int parseInt(final String s) {
        if (Objects.isNull(s) || s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (final Throwable ignore) {
            System.err.println("Could not parse value from _XY tag: " + s);
            return 0;
        }
    }

    private static String toName(final String name) {
        return name.replaceAll("/", "");
    }

    private static String toDate(final String date) {
        return date;
    }

    private static String getChildEventDate(final TreeNode<GedcomLine> node, final String tag) {
        for (final TreeNode<GedcomLine> c : node) {
            if (c.getObject().getTagString().equals(tag)) {
                return getChildValue(c, "DATE");
            }
        }
        return "";
    }

    private static String getChildValue(final TreeNode<GedcomLine> node, final String tag) {
        for (final TreeNode<GedcomLine> c : node) {
            if (c.getObject().getTagString().equals(tag)) {
                return c.getObject().getValue();
            }
        }
        return "";
    }

    private static Fami buildFami(final TreeNode<GedcomLine> nodeFami, final Map<String, Indi> mapIdToIndi) {
        final Fami fami = new Fami();
        for (final TreeNode<GedcomLine> c : nodeFami) {
            final GedcomLine child = c.getObject();
            switch (child.getTag()) {
                case HUSB:
                    fami.setHusb(mapIdToIndi.get(child.getPointer()));
                    break;
                case WIFE:
                    fami.setWife(mapIdToIndi.get(child.getPointer()));
                    break;
                case CHIL:
                    fami.addChild(mapIdToIndi.get(child.getPointer()));
                    break;
            }
        }
        return fami;
    }
}
