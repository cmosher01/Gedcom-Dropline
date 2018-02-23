package nu.mine.mosher.gedcom.dropline.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nu.mine.mosher.gedcom.dropline.Fami;
import nu.mine.mosher.gedcom.dropline.Indi;
import nu.mine.mosher.geom.Dim2D;


public class Layout {
    private static final int childdx = 18;
    private static final int familydx = childdx * 4;

    public Layout(final List<Indi> indis, final List<Fami> famis) {
        this.indis = IntStream.range(0, indis.size())
            .mapToObj(i -> new CIndividual(indis.get(i), i))
            .collect(Collectors.toList());

        final Map<Indi,CIndividual> mapIndis = new HashMap<>();
        this.indis.forEach(i -> mapIndis.put(i.indi,i));

        this.famis = IntStream.range(0, famis.size())
            .mapToObj(i -> new CFamily(famis.get(i), i))
            .collect(Collectors.toList());

        final Map<Fami,CFamily> mapFamis = new HashMap<>();
        this.famis.forEach(i -> mapFamis.put(i.fami,i));

        this.famis.forEach(f -> {
            final Fami fami = f.fami;

            final Optional<Indi> husb = fami.getHusb();
            final Optional<Indi> wife = fami.getWife();

            if (husb.isPresent()) {
                final CIndividual cHusb = mapIndis.get(husb.get());
                cHusb.ridxSpouseToFamily.add(f.idx);
                if (wife.isPresent()) {
                    cHusb.ridxSpouse.add(mapIndis.get(wife.get()).idx);
                }
            }
            if (wife.isPresent()) {
                final CIndividual cWife = mapIndis.get(wife.get());
                cWife.ridxSpouseToFamily.add(f.idx);
                if (husb.isPresent()) {
                    cWife.ridxSpouse.add(mapIndis.get(husb.get()).idx);
                }
            }

            fami.getChildren().forEach(c -> {
                final CIndividual cChild = mapIndis.get(c);
                cChild.idxChildToFamily = f.idx;
                f.children.add(cChild.idx);
                if (husb.isPresent()) {
                    final CIndividual cHusb = mapIndis.get(husb.get());
                    cHusb.ridxChild.add(mapIndis.get(c).idx);
                    cChild.idxFather = cHusb.idx;
                }
                if (wife.isPresent()) {
                    final CIndividual cWife = mapIndis.get(wife.get());
                    cWife.ridxChild.add(mapIndis.get(c).idx);
                    cChild.idxMother = cWife.idx;
                }
            });
        });
    }




    private final List<CIndividual> indis;
    private final List<CFamily> famis;

    class CFamily {
        private final int idx;
        private final Fami fami;

        /* indexes into indis list */
        private final List<Integer> children = new ArrayList<>();

        public CFamily(final Fami fami, final int idx) {
            this.fami = fami;
            this.idx = idx;
        }

        void GetSortedChildren(final List<Integer> riChild) {
            riChild.clear();
            riChild.addAll(children);
            riChild.sort(Comparator.comparing(i -> Layout.this.indis.get(i).GetSimpleBirth()));
        }
    }

    class CIndividual {
        private Layout m_pDoc = Layout.this;

        private int sex; // 0=unknown, 1=male, 2=female
        private Rectangle2D frame;

        private final Indi indi;
        /* indexes into indis list */
        private final int idx;
        private int idxFather = -1;
        private int idxMother = -1;
        private List<Integer> ridxSpouse = new ArrayList<>();
        private List<Integer> ridxChild = new ArrayList<>();
        /* indexes into famis list */
        private int idxChildToFamily = -1;
        private List<Integer> ridxSpouseToFamily = new ArrayList<>();

        private boolean mark;
        private int level;
        private int maxMale;
        private CIndividual house;



        public CIndividual(final Indi indi, final int idx) {
            this.indi = indi;
            this.idx = idx;
            this.sex = indi.getSex();
            this.frame = indi.getBounds();
        }

        void setLevel(final int lev) {
            //already done
            if (mark) {
                return;
            }

            //doing
            mark = true;

            //self
            level = lev;

            // position along y axis
            final int levdy = 100;
            final Point2D pt = new Point2D.Double(0.0D, (5000.0D - level) * levdy - frame.getHeight() / 2);
            MoveTo(pt);

            //father
            if (idxFather >= 0) {
                m_pDoc.indis.get(idxFather).setLevel(lev + 1);
            }
            //mother (only if no father)
            else if (idxMother >= 0) {
                m_pDoc.indis.get(idxMother).setLevel(lev + 1);
            }

            //siblings
            if (idxChildToFamily >= 0) {
                final CFamily fami = m_pDoc.famis.get(idxChildToFamily);
                for (int i = 0; i < fami.children.size(); ++i) {
                    final CIndividual sib = m_pDoc.indis.get(fami.children.get(i));
                    sib.setLevel(lev);
                }
            }

            //children
            for (final Integer i : ridxChild) {
                m_pDoc.indis.get(i).setLevel(lev - 1);
            }

            //spouses
            for (final Integer i : ridxSpouse) {
                m_pDoc.indis.get(i).setLevel(lev);
            }
        }

        void MoveTo(final Point2D pt) {
            this.frame.setRect(pt.getX(), pt.getY(), this.frame.getWidth(), this.frame.getHeight());
        }

        void setMaxMaleIf(final int n) {
            if (maxMale < n) {
                maxMale = n;
            }
        }

        void setRootWithSpouses(final CIndividual proot) {
            if (mark) {
                return;
            }

            final List<CIndividual> all_sps = new ArrayList<>();
            // build list of all spouses in this spouse group
            {
                final LinkedList<CIndividual> todo = new LinkedList<>();
                todo.addLast(this);
                while (!todo.isEmpty()) {
                    final CIndividual pthis = todo.removeFirst();
                    all_sps.add(pthis);
                    for (int sp = 0; sp < pthis.ridxSpouse.size(); ++sp) {
                        final CIndividual pspou = m_pDoc.indis.get(pthis.ridxSpouse.get(sp));
                        if (/*pspou->level == level &&*/!pspou.mark && !all_sps.contains(pspou)) {
                            todo.addLast(pspou);
                        }
                    }
                }
            }

            for (final CIndividual pindi2 : all_sps) {
                if (pindi2 == this || pindi2.idxFather < 0) {
                    pindi2.house = proot;
                    pindi2.mark = true;
                }
            }
        }

        void setSeqWithSpouses(final List<Pair<Double, Double>> lev_bounds, final boolean left, final List<CIndividual> cleannext) {
            final LinkedList<CIndividual> all_sps = new LinkedList<>();
            // build list of all spouses in this spouse group
            {
                final LinkedList<CIndividual> todo = new LinkedList<>();
                if (!mark) {
                    todo.addLast(this);
                }
                while (!todo.isEmpty()) {
                    final CIndividual pthis = todo.removeFirst();
                    all_sps.add(pthis);
                    for (int sp = 0; sp < pthis.ridxSpouse.size(); ++sp) {
                        final CIndividual pspou = m_pDoc.indis.get(pthis.ridxSpouse.get(sp));
                        if (/*pspou->level == level &&*/!pspou.mark && !all_sps.contains(pspou)) {
                            todo.addLast(pspou);
                        }
                    }
                }
            }

            final Iterator<CIndividual> i = all_sps.descendingIterator();
            while (i.hasNext()) {
                final CIndividual indi = i.next();
                if (indi.idxFather >= 0) {
                    final CIndividual parent = m_pDoc.indis.get(indi.idxFather);
                    if (parent.house != null && parent.house != house) {
                        cleannext.add(parent.house);
                    }
                }
                if (indi.idxMother >= 0) {
                    final CIndividual parent = m_pDoc.indis.get(indi.idxMother);
                    if (parent.house != null && parent.house != house) {
                        cleannext.add(parent.house);
                    }
                }
            }

            if (mark) {
                return;
            }

            LinkedList<CIndividual> left_sps = new LinkedList<>();
            // build list of spouses to be displayed off to the LEFT of the indi
            {
                left_sps.add(this);
                CIndividual pindi = this;
                while (pindi != null) {
                    boolean found = false;
                    for (int sp = 0; !found && sp < pindi.ridxSpouse.size(); ++sp) {
                        final CIndividual pspou = m_pDoc.indis.get(pindi.ridxSpouse.get(sp));
                        if (!pspou.mark && pspou.idxFather < 0 && pspou != this && !left_sps.contains(pspou)) {
                            found = true;
                            pindi = pspou;
                            left_sps.add(pspou);
                        }
                    }
                    if (!found) {
                        pindi = null;
                    }
                }
            }

            LinkedList<CIndividual> right_sps = new LinkedList<>();
            // build list of spouses to be displayed off to the RIGHT of the indi
            {
                // TODO this is inconsistent with left, because this was missing: right_sps.add(this);  is this correct????
                CIndividual pindi = this;
                while (pindi != null) {
                    boolean found = false;
                    for (int sp = 0; !found && sp < pindi.ridxSpouse.size(); ++sp) {
                        final CIndividual pspou = m_pDoc.indis.get(pindi.ridxSpouse.get(sp));
                        if (!pspou.mark && pspou.idxFather < 0 && pspou != this && !left_sps.contains(pspou) && !right_sps.contains(pspou)) {
                            found = true;
                            pindi = pspou;
                            right_sps.add(pspou);
                        }
                    }
                    if (!found) {
                        pindi = null;
                    }
                }
            }
            //add (to the right) all remaining spouses
            for (final CIndividual pspou : all_sps) {
                if (!pspou.mark && pspou.idxFather < 0 && !left_sps.contains(pspou) && !right_sps.contains(pspou)) {
                    right_sps.add(pspou);
                }
            }

            if (!left) {
                final LinkedList<CIndividual> t = right_sps;
                left_sps = right_sps;
                right_sps = t;
            }

            // display spouses from the LEFT
            final Iterator<CIndividual> left_iter = left_sps.descendingIterator();
            //noinspection WhileLoopReplaceableByForEach
            while (left_iter.hasNext()) {
                displaySpouses(lev_bounds, left_iter.next());
            }

            // display spouses to the RIGHT
            final Iterator<CIndividual> right_iter = right_sps.iterator();
            //noinspection WhileLoopReplaceableByForEach
            while (right_iter.hasNext()) {
                displaySpouses(lev_bounds, right_iter.next());
            }
        }

        private void displaySpouses(final List<Pair<Double, Double>> lev_bounds, final CIndividual pindi) {
            pindi.MoveTo(new Point2D.Double(lev_bounds.get(pindi.level).second, pindi.frame.getY()));
            pindi.mark = true;
            lev_bounds.get(pindi.level).second = pindi.frame.getMaxX() + childdx;
        }

        Date GetSimpleBirth() {
            return new Date(); // TODO
        }
    }






    public void cleanAll() {
        // preliminary stuff
        final int cIndi = this.indis.size();
        if (cIndi <= 1) {
            return;
        }



        // set generation levels (also sets position on y-axis)
        ClearAllIndividuals();
        {
            int batch = 0;
            boolean someleft = true;
            while (someleft) {
                someleft = false;
                for (final CIndividual indi : this.indis) {
                    if (!indi.mark) {
                        someleft = true;
                        indi.setLevel(batch++ * 20);
                    }
                }
            }
        }

        // find number of levels, and normalize indis' level nums
        int nlev;
        {
            // Counting generations
            int maxlev = Integer.MIN_VALUE;
            int minlev = Integer.MAX_VALUE;
            for (final CIndividual indi : this.indis) {
                final int lev = indi.level;
                if (maxlev < lev) {
                    maxlev = lev;
                }
                if (lev < minlev) {
                    minlev = lev;
                }
            }
            //count of levels
            nlev = maxlev - minlev + 1;
            for (final CIndividual indi : this.indis) {
                indi.level -= minlev;
            }
        }



        // calc max male-branch-descendant-generations size for all indis
        ClearAllIndividuals();
        {
            // Finding branches
            for (final CIndividual pindi : this.indis) {
                CIndividual pfath = pindi;
                int c;
                if (pindi.sex == 1) {
                    c = 1;
                } else {
                    c = 0;
                }
                final Set<Integer> setindi = new HashSet<>();// guard against loops
                int f;
                while (((f = pfath.idxFather) >= 0) && !setindi.contains(f)) {
                    setindi.add(f);
                    ++c;
                    pfath = this.indis.get(f);
                }
                pfath.setMaxMaleIf(c);
                if (pfath.idxMother >= 0) {
                    this.indis.get(pfath.idxMother).maxMale = c + 1;
                }
            }
        }



        final LinkedList<CIndividual> rptoclean = new LinkedList<>();
        {
            // build vector of indis (w/male descent size)
            final List<Pair<Integer, CIndividual>> rma = new ArrayList<>();
            for (int i = 0; i < cIndi; ++i) {
                final CIndividual pindi = this.indis.get(i);
                if (pindi.maxMale != 0) {
                    rma.add(new Pair<>(pindi.maxMale, pindi));
                }
            }

            // Sorting branches.
            rma.sort((
                (Comparator<Pair<Integer, CIndividual>>) (o1, o2) -> {
                    int c = o1.first.compareTo(o2.first);
                    if (c == 0) {
                        c = Integer.compare(o1.second.level, o2.second.level);
                    }
                    if (c == 0) {
                        c = Integer.compare(o1.second.sex, o2.second.sex);
                    }
                    return c;
                }).reversed());

            // put indis on rptoclean list in order of processing
            rma.forEach(ma -> rptoclean.add(ma.second));
        }



        // Labeling branches

        ClearAllIndividuals();

        for (final CIndividual pindi : rptoclean) {
            final LinkedList<CIndividual> todo = new LinkedList<>();

            pindi.setRootWithSpouses(pindi);
            todo.addLast(pindi);
            while (!todo.isEmpty()) {
                final CIndividual pgmi = todo.removeFirst();
                for (int j = 0; j < pgmi.ridxSpouseToFamily.size(); ++j) {
                    final CFamily fami = this.famis.get(pgmi.ridxSpouseToFamily.get(j));
                    for (int k = 0; k < fami.children.size(); ++k) {
                        final CIndividual pchil = this.indis.get(fami.children.get(k));
                        if (!pchil.mark) {
                            pchil.setRootWithSpouses(pindi);
                            if (pchil.sex == 1) {
                                todo.addLast(pchil);
                            }
                        }
                    }
                }
            }
        }



        // build new list with only house heads
        final LinkedList<CIndividual> rptoclean2 = new LinkedList<>();
        final Set<CIndividual> settoclean2 = new HashSet<>();
        {
            // Finding progenitors
            //make a list of all house heads
            final Set<Integer> setheads = new HashSet<>();
            for (final CIndividual pindi : this.indis) {
                if (pindi.house != null) {
                    setheads.add(pindi.house.idx);
                }
            }

            // put house heads on rptoclean2 list in order of processing
            for (final CIndividual psec : rptoclean) {
                if (setheads.contains(psec.idx)) {
                    rptoclean2.add(psec);
                    settoclean2.add(psec);
                }
            }
        }



        final List<Pair<Double, Double>> lev_bounds = new ArrayList<>();
        for (int i = 0; i < nlev; ++i) {
            lev_bounds.add(new Pair<>(999.999D /* unused??? */, 0.0D));
        }

        ClearAllIndividuals();
        // Moving branches.
        while (!rptoclean2.isEmpty()) {
            final CIndividual psec = rptoclean2.remove(0);
            settoclean2.remove(psec);

            final List<CIndividual> nexthouse = new ArrayList<>();

            final Set<CIndividual> guard = new HashSet<>();
            final List<CIndividual> todo = new ArrayList<>();
            todo.add(psec);
            guard.add(psec);
            while (!todo.isEmpty()) {
                final CIndividual pgmi = todo.remove(0);
                final List<CIndividual> cleannext = new ArrayList<>();
                pgmi.setSeqWithSpouses(lev_bounds, false, cleannext);
                nexthouse.addAll(cleannext);

                for (int j = 0; j < pgmi.ridxSpouseToFamily.size(); ++j) {
                    final CFamily fami = this.famis.get(pgmi.ridxSpouseToFamily.get(j));
                    final List<Integer> riChild = new ArrayList<>();
                    fami.GetSortedChildren(riChild);
                    int nch = riChild.size();
                    if (nch > 0) {
                        //put the (first two) children with spouses on the outside edges
                        int sp1 = -1;
                        int sp2 = -1;
                        for (int ch = 0; ch < nch; ++ch) {
                            final CIndividual chil = this.indis.get(riChild.get(ch));
                            if (!chil.ridxSpouse.isEmpty()) {
                                if (sp1 < 0) {
                                    sp1 = ch;
                                } else if (sp2 < 0) {
                                    sp2 = ch;
                                }
                            }
                        }

                        final List<Integer> riChild2 = new ArrayList<>();
                        if (sp1 >= 0) {
                            riChild2.add(riChild.get(sp1));
                        }
                        for (int ch = 0; ch < nch; ++ch) {
                            if (ch != sp1 && ch != sp2) {
                                riChild2.add(riChild.get(ch));
                            }
                        }
                        if (sp2 >= 0) {
                            riChild2.add(riChild.get(sp2));
                        }
                        nch = riChild2.size();

                        boolean left = (nch > 1);
                        for (int k = 0; k < nch; ++k) {
                            final CIndividual pchil = this.indis.get(riChild2.get(k));
                            final List<CIndividual> cleannext2 = new ArrayList<>();
                            pchil.setSeqWithSpouses(lev_bounds, left, cleannext2);
                            nexthouse.addAll(cleannext2);
                            lev_bounds.get(pchil.level).second += childdx;
                            left = false;
                            if (pchil.sex == 1 && !guard.contains(pchil)) {
                                todo.add(pchil);
                                guard.add(pchil);
                            }
                        }
                    }
                }
            }

            double maxx = Double.NEGATIVE_INFINITY;
            boolean any = false;
            for (int j = 0; j < nlev; ++j) {
                if (maxx < lev_bounds.get(j).second) {
                    maxx = lev_bounds.get(j).second;
                }
                //kludge to see if any people in this house
                if (j > 0 && !lev_bounds.get(j).second.equals(lev_bounds.get(j - 1).second)) {
                    any = true;
                }
            }
            if (any) {
                maxx += familydx;
                for (int j = 0; j < nlev; ++j) {
                    lev_bounds.get(j).second = maxx;
                }
            }

            for (final CIndividual pindi : nexthouse) {
                if (settoclean2.contains(pindi)) {
                    rptoclean2.remove(pindi);
                    rptoclean2.add(pindi);
                }
            }
        }

        this.indis.forEach(i -> {
            i.indi.move(new Dim2D(i.frame.getX(), i.frame.getY()));
        });
    }

    private void ClearAllIndividuals() {
        this.indis.forEach(i -> i.mark = false);
    }






    static class Pair<T, U> {
        T first; //mutable
        U second; //mutable

        Pair(final T i, final U j) {
            this.first = i;
            this.second = j;
        }

        @Override
        public int hashCode() {
            return first.hashCode() ^ second.hashCode();
        }

        @Override
        public boolean equals(final Object object) {
            if (!(object instanceof Pair)) {
                return false;
            }
            final Pair that = (Pair) object;
            return first.equals(that.first) && second.equals(that.second);
        }
    }
}
