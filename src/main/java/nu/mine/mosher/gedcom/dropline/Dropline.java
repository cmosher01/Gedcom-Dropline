package nu.mine.mosher.gedcom.dropline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author Chris Mosher
 */
public class Dropline extends JApplet {
    private boolean test = false;
    private FamilyChart fc;

    /**
     * @throws HeadlessException
     */
    public Dropline() throws HeadlessException {
    }

    /**
     *
     */
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

        InputStream streamTree;

        if (this.test) {
            streamTree = new FileInputStream(new File("test.gro"));
        } else {
            URL url = new URL(getDocumentBase(), "?chartdata");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            streamTree = con.getInputStream();
        }

        readFrom(streamTree);

        JScrollPane scr = new JScrollPane(fc, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scr);
    }

    protected void readFrom(InputStream instream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(instream, "UTF-8"));

        Map<String, Indi> mapIdToIndi = new HashMap<String, Indi>();

        IndiSet indis = new IndiSet();

        String sMaxWidth = br.readLine();
        double dMaxWidth = Double.parseDouble(sMaxWidth);
        int cMaxWidth = (int) Math.round(dMaxWidth);
        indis.setMaxWidth(cMaxWidth);

        String scIndi = br.readLine();
        int cIndi = Integer.parseInt(scIndi);
        for (int i = 0; i < cIndi; ++i) {
            String slineIndi = br.readLine();
            StringFieldizer sf = new StringFieldizer(slineIndi);
            StringFieldizer.Iter it = sf.iterator();
            String id = it.next();
            int nid = Integer.parseInt(id.substring(1));
            String name = it.next();
            String birth = it.next();
            String death = it.next();
            String sx = it.next();
            double dx = Double.parseDouble(sx);
            int x = (int) Math.round(dx);
            String sy = it.next();
            double dy = Double.parseDouble(sy);
            int y = (int) Math.round(dy);
            Indi indi = new Indi(x, y, nid, name, birth, death);
            indis.add(indi);
            mapIdToIndi.put(id, indi);
        }

        FamiSet famis = new FamiSet();
        String scFami = br.readLine();
        int cFami = Integer.parseInt(scFami);
        for (int i = 0; i < cFami; ++i) {
            String slineIndi = br.readLine();
            StringFieldizer sf = new StringFieldizer(slineIndi);
            StringFieldizer.Iter it = sf.iterator();
            Fami fami = new Fami();
            String husb = it.next();
            fami.setHusb(mapIdToIndi.get(husb));
            String wife = it.next();
            fami.setWife(mapIdToIndi.get(wife));
            String sc = it.next();
            int c = Integer.parseInt(sc);
            for (int ic = 0; ic < c; ++ic) {
                String chil = it.next();
                fami.addChild(mapIdToIndi.get(chil));
            }
            famis.add(fami);
        }

        fc = new FamilyChart(this, indis, famis);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.println("Arguments ignored.");
        }

        Frame f = new Frame("Paint Applet");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Dropline applet = new Dropline();
        applet.setTestMode();
        applet.init();
        f.add(applet);

        f.setSize(640, 480);
        f.setVisible(true);
    }

    private void setTestMode() {
        this.test = true;
    }
}
