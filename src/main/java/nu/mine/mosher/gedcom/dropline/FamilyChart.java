package nu.mine.mosher.gedcom.dropline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FamilyChart {
    private final List<Indi> indis;
    private final List<Fami> famis;

    public FamilyChart(final List<Indi> indis, final List<Fami> famis) {
        this.indis = Collections.unmodifiableList(new ArrayList<>(indis));
        this.famis = Collections.unmodifiableList(new ArrayList<>(famis));
    }

    public void buildInto(final SvgBuilder svg) {
        this.indis.forEach(i -> i.buildInto(svg));
        this.famis.forEach(f -> f.buildInto(svg));
    }
}
