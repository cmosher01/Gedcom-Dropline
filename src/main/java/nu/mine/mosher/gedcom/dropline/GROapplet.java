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
public class GROapplet extends JApplet {
    private boolean test = false;
    private FamilyChart fc;

    /**
     * @throws HeadlessException
     */
    public GROapplet() throws HeadlessException {
    }

    /**
     *
     */
    public void init() {
        try {
            super.init();
            tryinit();
        } catch (Throwable e) {
            handleException(e);
        }
    }

    protected void handleException(Throwable e) {
        try {
            /*
             * Print simple error message to Java Console.
             * Do this for sanity in case something bad
             * happens below that prevents any error message
             * from being displayed in the browser.
             */
            e.printStackTrace();

            /*
             * Build a StringBuffer containing the entire
             * exception message and stack dump. This includes
             * any chained exceptions.
             */
            StringBuffer sb = new StringBuffer();
            appendException(e, sb);
            Throwable cause = e.getCause();
            while (cause != null) {
                sb.append("caused by:\r\n");
                appendException(cause, sb);
                cause = cause.getCause();
            }

            /*
             * Make a text area (with scroll bars as needed)
             * and display the exception message.
             */
            JTextArea errorPane = new JTextArea();
            errorPane.read(new StringReader(sb.toString()), null);
            JScrollPane areaScrollPane = new JScrollPane(errorPane);
            areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            getContentPane().removeAll();
            getContentPane().add(areaScrollPane);
        } catch (Throwable ignore) {
            System.err.println("Exception happened while processing exception.");
            System.err.println(ignore.getMessage());
            System.err.println("The original exception was:");
            System.err.println(e.getMessage());
        }
    }

    protected static void appendException(Throwable e, StringBuffer sb) {
        String sMsg = e.getMessage();
        if (sMsg != null) {
            sb.append(sMsg);
            sb.append("\r\n");
        }

        sb.append(e.getClass().getName());
        sb.append("\r\n");

        StackTraceElement[] rtr = e.getStackTrace();
        for (int i = 0; i < rtr.length; i++) {
            StackTraceElement stackTraceElement = rtr[i];
            sb.append("    at ");
            sb.append(stackTraceElement.toString());
            sb.append("\r\n");
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

        //		if (test)
        //		{
        //			streamTree = new FileInputStream(new File("test.gro"));
        //		}
        //		else
        //		{
        URL url = new URL(getDocumentBase(), "?chartdata");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.connect();
        streamTree = con.getInputStream();
        //		}

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

        GROapplet applet = new GROapplet();
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
