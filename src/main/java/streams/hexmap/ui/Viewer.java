package streams.hexmap.ui;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import streams.cta.CTATelescope;
import streams.hexmap.CTAHexPixelMapping;
import streams.hexmap.ui.components.AveragePlotPanel;
import streams.hexmap.ui.components.EventInfoPanel;
import streams.hexmap.ui.components.StreamNavigationPanel;
import streams.hexmap.ui.components.cameradisplay.CameraDisplayPanel;
import streams.hexmap.ui.events.ItemChangedEvent;
import streams.hexmap.ui.windows.CameraWindow;
import streams.hexmap.ui.windows.PlotDisplayWindow;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

/**
 * Here the components of the gui are layed out. like the position of the camera map or the chart display in the window.
 * An EventBus is used to communicate with some of the components. In case a new data item from the stream
 * is set  a new Event will be send out and every subscriber (for example the mapDisplay) can react to it.
 * See the setDataItem(Data item)
 *
 * @author kai
 */
public class Viewer extends JFrame {

	static Logger log = LoggerFactory.getLogger(Viewer.class);


    //------some components for the viewer
    final CameraDisplayPanel mapDisplay = new CameraDisplayPanel(CTAHexPixelMapping.getInstance());
    final StreamNavigationPanel navigation = new StreamNavigationPanel();
    final AveragePlotPanel chartPanel = new AveragePlotPanel(550, 350);
    final EventInfoPanel eventInfoPanel = new EventInfoPanel(600, 320);


    private String defaultKey = "@event";


    //set plotrange in the plotpanel
    public void setRange(Integer[] range){
        chartPanel.setRange(range[0], range[1]);
    }

    private Data item;


    //there should also be only 1 instance of the viewer.
    private static Viewer viewer = null;
    public static Viewer getInstance() {
        if (viewer == null) {
            viewer = new Viewer();
        }
        return viewer;
    }

    //the constructor. build layout here. bitch
	private Viewer() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Fact Tools GUI Development");


        //------- add a chart window
        //chartPanel.setBackground(Color.WHITE);
        //chartPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null,
        //        null, null, null));
        JMenuBar menu = createMenuBar();
        this.setJMenuBar(menu);

        // set layout of the main window
        FormLayout layout = new FormLayout(new ColumnSpec[] {
                                    ColumnSpec.decode("fill:605px"),
                                    ColumnSpec.decode("fill:605px"), },
                                new RowSpec[] {
                                    RowSpec.decode("fill:pref"),
                                    RowSpec.decode("fill:pref"),
                                    RowSpec.decode("fill:pref")
                                });

        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        builder.add(chartPanel, cc.xywh(2, 1, 1, 1));
        builder.add(eventInfoPanel, cc.xywh(2, 2, 1, 1));
        builder.add(mapDisplay, cc.xywh(1, 1, 1, 2));
        builder.add(navigation, cc.xywh(1,3, 2,1));

        setContentPane(builder.getPanel());

		//setSize(1200, 850);
		pack();
	}


    /**
     * Creates the menu bar and returns it. All menubar setup happens in here
     * @return the menubar created
     */
    private JMenuBar createMenuBar() {
        //---add a menu bar on top
        //in case of mac os use the system native menu bar.
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        JMenuBar menu = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(e -> System.exit(0));
        file.add(quit);

        //----- WINDOWS---
        JMenu windows = new JMenu("Windows");
        JMenuItem camWindowMenuItem = new JMenuItem("New Camera Window");
        camWindowMenuItem.addActionListener(e -> {
            CameraWindow mw = new CameraWindow(defaultKey, CTAHexPixelMapping.getInstance());
            Bus.eventBus.post(Pair.create(item, defaultKey));
            mw.showWindow();
        });
        JMenuItem plotWindowItem = new JMenuItem("New Plot Window");
        plotWindowItem.addActionListener(e -> {
            PlotDisplayWindow plotDisplay = new PlotDisplayWindow();
            Bus.eventBus.post(Pair.create(item, defaultKey));
            plotDisplay.showWindow();
        });

        windows.add(plotWindowItem);
        windows.add(camWindowMenuItem);

        //------- HELP--------
        JMenu help = new JMenu("Help");
        JMenuItem visitWeb = new JMenuItem("Visit FactTools Website");
        visitWeb.addActionListener(e -> openUrl("http://sfb876.tu-dortmund.de/FACT/"));
        JMenuItem visitStream = new JMenuItem("Visit StreamsFramework Website");
        visitStream.addActionListener(e -> openUrl("http://www.jwall.org/streams/"));
        help.add(visitStream);
        help.add(visitWeb);


        menu.add(file);
        menu.add(windows);
        menu.add(help);
        menu.setBackground(Color.GRAY);
        return menu;
    }

    /**
     * Open the default web browser of the system with the specified url
     * stolen from http://stackoverflow.com/questions/10967451/open-a-link-in-browser-with-java-button
     * @param url the url to open
     */
    public void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    java.net.URI uri = new java.net.URI(url);
                    desktop.browse(uri);
                }
            }
        } catch (IOException e){
            log.error("Couldnt connect to desktop environment. Cannot open browser");
        } catch (URISyntaxException e) {
            log.error("Wrong syntax for an url provided by string: " + url);
        }

    }



	public JButton getNextButton() {
		return navigation.getNextButton();
	}


    /**
     * The current data item to be displayed. This will sent an Event to all eventbus subscribers who care about new
     * events from the stream
     * @param item the new item from the stream
     */
	public void setDataItem(Data item, LocalDateTime timeStamp, CTATelescope telescope, short[][] rawData) {
        this.item = item;
        double[][] data = new double[rawData.length][rawData[0].length];
        for (int pixel = 0; pixel < rawData.length; pixel++) {
            for (int slice = 0; slice < rawData[0].length; slice++) {
                data[pixel][slice] = rawData[pixel][slice];
            }
        }
        this.chartPanel.drawPlot(data);
        Bus.eventBus.post(new ItemChangedEvent(item, timeStamp, telescope, rawData ));
	}

}