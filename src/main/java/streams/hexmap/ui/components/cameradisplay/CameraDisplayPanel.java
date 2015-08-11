package streams.hexmap.ui.components.cameradisplay;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import streams.hexmap.GifSequenceWriter;
import streams.hexmap.HexPixelMapping;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.EventObserver;
import streams.hexmap.ui.colormaps.ColorMapping;
import streams.hexmap.ui.components.selectors.CameraOverlayKeySelector;
import streams.hexmap.ui.events.ItemChangedEvent;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * This panel contains the hexmap with the overlay selector below it. The colorbar is part of the hexmap.
 * 
 * Created by kaibrugge on 02.06.14.
 */
public class CameraDisplayPanel extends JPanel implements EventObserver {

	static Logger log = LoggerFactory.getLogger(CameraDisplayPanel.class);

	final HexMapDisplay hexmap;
	final CameraOverlayKeySelector selector = new CameraOverlayKeySelector();
	private final Set<Class<? extends ColorMapping>> colorMapClasses;


	/**
	 * Adds the keys we can display in the plot window to the list on right side
	 * of the screen.
	 *
	 * @param itemChangedEvent
	 *            the current data item we want to display
	 */
	@Override
	@Subscribe
	public void handleEventChange(ItemChangedEvent itemChangedEvent) {
//		hexmap.setOverlayItemsToDisplay(selector.getSelectedPlotData());
		// hexmap.handleEventChange(itemKeyPair);
	}



	public CameraDisplayPanel(HexPixelMapping hexPixelMapping) {
        hexmap = new HexMapDisplay(0.7, 600, 530, hexPixelMapping);
		Bus.eventBus.register(this);

		// get all classes that implement the colormapping interface
		Reflections reflections = new Reflections("streams");
		colorMapClasses = reflections.getSubTypesOf(ColorMapping.class);

		// setup the hexmap component of the viewer
		hexmap.setBackground(Color.BLACK);

		// --------action listeners for menus and buttons----------

		// actionlistener for context menu.
		ActionListener contextMenuListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equalsIgnoreCase("Patch selection")) {
					AbstractButton b = (AbstractButton) e.getSource();
					hexmap.setPatchSelectionMode(b.isSelected());
				}
				// export to .png
				if (e.getActionCommand().equalsIgnoreCase("Export to .png")) {
					exportPNG();
					return;
				}

				if (e.getActionCommand().equalsIgnoreCase("Export to .gif")) {
					exportGIF();
					return;
				}

				// select the colormap
				for (Class<? extends ColorMapping> mapClass : colorMapClasses) {
					if (e.getActionCommand().equals(mapClass.getSimpleName())) {
						try {
							hexmap.setColorMap(mapClass.newInstance());
						} catch (InstantiationException e1) {
							log.error("Caught InstantiationException while trying to add new colormap with name: "
									+ mapClass.getSimpleName()
									+ ".  Colormaps must have a 0 argument constructor "
									+ "(nullary constructor)");
						} catch (IllegalAccessException e1) {
							log.error("Caught IllegalAccessException while trying to add new colormap with name: "
									+ mapClass.getSimpleName()
									+ ".  Constructor Private?");
						}
					}
				}
			}
		};

		// Build a context menu for color mapping and add it to the hexmap
		JPopupMenu popupMenu = new JPopupMenu("Color Mapping");
		for (Class<? extends ColorMapping> map : colorMapClasses) {
			JMenuItem colorMapMenuItem1 = new JMenuItem(map.getSimpleName());
			colorMapMenuItem1.addActionListener(contextMenuListener);
			popupMenu.add(colorMapMenuItem1);
		}
		// Add the menu item to export the file to .png
		popupMenu.addSeparator();
		JMenuItem exportItem = new JMenuItem("Export to .png");
		exportItem.addActionListener(contextMenuListener);
		popupMenu.add(exportItem);

		JMenuItem exportGIFItem = new JMenuItem("Export to .gif");
		exportGIFItem.addActionListener(contextMenuListener);
		popupMenu.add(exportGIFItem);

		popupMenu.addSeparator();
		JCheckBoxMenuItem patchSelectionMenuItem = new JCheckBoxMenuItem(
				"Patch selection");
		patchSelectionMenuItem.addActionListener(contextMenuListener);
		popupMenu.add(patchSelectionMenuItem);

		hexmap.setComponentPopupMenu(popupMenu);
		selector.setPreferredSize(new Dimension(600, 120));

		// set layout of the main window
		FormLayout layout = new FormLayout(
				new ColumnSpec[] { ColumnSpec.decode("left:pref:grow"), },
				new RowSpec[] { RowSpec.decode("fill:530"),
						RowSpec.decode("center:10dlu:grow"),
						RowSpec.decode("fill:125"), });

		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		// first row
		builder.add(hexmap, cc.xy(1, 1));
		builder.addSeparator("Overlays", cc.xy(1, 2));

		builder.add(selector, cc.xy(1, 3));
		// builder.add(overlaySelector, cc.xywh(1,4,6,1));
		add(builder.getPanel());

	}

	/**
	 * This method shows a jfilechooser to save the current hexmap image as a
	 * png file.
	 */
	private void exportPNG() {
		// draw stuff again
		BufferedImage bi = new BufferedImage(this.getSize().width,
				this.getSize().height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.createGraphics();
		hexmap.paint(g, true);
		g.dispose();

		// open a file chooser for png files
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				".png Images", ".png");

		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(filter);
		int ret = chooser.showSaveDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
			// if there isn't already the .png extension, add it.
			File f = chooser.getSelectedFile();
			if (!f.getAbsolutePath().endsWith(".png")) {
				f = new File(f + ".png");
			}
			// now write the file
			try {
				ImageIO.write(bi, "png", f);
			} catch (IOException e) {
				e.printStackTrace();
				log.error("Couldn't write image. Is the path writable?");
			}
		}
	}

	public void exportGIF() {
		// open a file chooser for png files
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				".gif Images", ".gif");

		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(filter);
		int ret = chooser.showSaveDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
            try {
                ImageOutputStream output = new FileImageOutputStream(chooser.getSelectedFile());

                GifSequenceWriter writer = new GifSequenceWriter(output,
                        BufferedImage.TYPE_INT_ARGB, 2, true);

                for (int s = 25; s < 225; s++) {
                    try {
                        hexmap.currentSlice = s;
                        System.out.println("Painting slice " + s);
                        BufferedImage bi = new BufferedImage(getSize().width,
                                getSize().height, BufferedImage.TYPE_INT_ARGB);
                        Graphics g = bi.createGraphics();
                        hexmap.paint(g, true);
                        g.dispose();

                        writer.writeToSequence(bi);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }

                writer.close();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

    //TODO: check if we need this method for the camerawindow
    public void setItemToDisplay(String key, Data dataItem) {

    }
}
