package com.isti.traceview.gui.controls;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.isti.traceview.TraceView;
import com.isti.traceview.common.TimeInterval;
import com.isti.traceview.gui.GraphPanel;
import com.isti.traceview.gui.GraphUtil;

/**
 * Set of buttons to zoom in time axis traces into GraphPanel. Has "Zoom", "Unzoom" and auxilary
 * "All data" buttons.
 * 
 * @author Max Kokoulin
 */
public class Zoomer extends JPanel implements ActionListener {
	private static Logger lg = Logger.getLogger(Zoomer.class);
	private static Insets inset = new Insets(0, 0, 0, 0);
	private static final Dimension maxSize = new Dimension(130, 30);
	private double factor = 1.5;
	private GraphPanel graphPanel = null;
	private JButton zoomInButton = null;
	private JButton zoomOutButton = null;
	private JButton allData = null;
	private JButton oneDayButton = null;

	/**
	 * @param graphPanel
	 *            Graph panel which this element controls
	 * @param factor
	 *            multiplier to zoom/unzoom window
	 * @param showAllData
	 *            flag if we show "All data" button
	 */
	public Zoomer(GraphPanel graphPanel, double factor, boolean showAllData, boolean show1Day) {
		super();
		setPreferredSize(maxSize);
		setMaximumSize(maxSize);
		int fields = 2;
		if (showAllData) {
			fields++;
		} if (show1Day) {
			fields++;
		}
		setLayout(new GridLayout(0, fields));
		//setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.graphPanel = graphPanel;
		if (factor > 1.0) {
			this.factor = factor;
		} else {
			lg.error("Zoom factor = " + factor + " must be greater than 1; Forced to " + this.factor);
		}
		zoomInButton = GraphUtil.createGraphicalButton("zoom_in.png", "Zoom In");
		zoomInButton.setToolTipText("Zoom In");
		zoomInButton.addActionListener(this);
		zoomInButton.setBorderPainted(true);
		zoomInButton.setMargin(inset);

		zoomOutButton = GraphUtil.createGraphicalButton("zoom_out.png", "Zoom Out");
		zoomOutButton.setToolTipText("Zoom Out");
		zoomOutButton.setBorderPainted(true);
		zoomOutButton.addActionListener(this);
		zoomOutButton.setMargin(inset);

		add(zoomInButton);
		add(zoomOutButton);
		if (showAllData) {
			allData = GraphUtil.createGraphicalButton("all_data.gif", "All");
			allData.setToolTipText("Show all data");
			allData.setBorderPainted(true);
			allData.addActionListener(this);
			allData.setMargin(inset);
			add(allData);
		}
		if (show1Day) {
			oneDayButton = GraphUtil.createGraphicalButton("1day.png", "Day");
			oneDayButton.setToolTipText("Show 1 Day");
			oneDayButton.setBorderPainted(true);
			oneDayButton.addActionListener(this);
			oneDayButton.setMargin(inset);
			add(oneDayButton);
		}
	}

	/**
	 * Simplified constructor
	 * 
	 * @param graphPanel
	 *            Graph panel which this element controls
	 */
	public Zoomer(GraphPanel graphPanel) {
		this(graphPanel, 1.5, false, false);
	}

	public void actionPerformed(ActionEvent e) {
		long centerTime = (graphPanel.getTimeRange().getStart() + graphPanel.getTimeRange().getEnd()) / 2;
		long duration = graphPanel.getTimeRange().getDuration();
		if (e.getSource().equals(zoomInButton)) {
			graphPanel.setTimeRange(new TimeInterval(centerTime - new Double(duration / (2 * factor)).longValue(), centerTime
					+ new Double(duration / (2 * factor)).longValue()));
		} else if (e.getSource().equals(zoomOutButton)) {
			graphPanel.setTimeRange(new TimeInterval(centerTime - new Double(duration * factor / 2).longValue(), centerTime
					+ new Double(duration * factor / 2).longValue()));
		} else if (e.getSource().equals(allData)) {
			graphPanel.setTimeRange(TraceView.getDataModule().getAllDataTimeInterval());
		} else if (e.getSource().equals(oneDayButton)) {
			graphPanel.setTimeRange(DurationSelector.getDayRange(graphPanel.getTimeRange().getStartTime(), 1));
		}
	}
	
	public void addKeyListener(KeyListener l){
		zoomInButton.addKeyListener(l);
    	zoomOutButton.addKeyListener(l);
    	if(allData!=null){
    		allData.addKeyListener(l);
    	}
    	if(oneDayButton!=null){
    		oneDayButton.addKeyListener(l);
    	}
	}
	
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	zoomInButton.setEnabled(enabled);
    	zoomOutButton.setEnabled(enabled);
    	if(allData!=null){
    		allData.setEnabled(enabled);
    	}
    	if(oneDayButton!=null){
    		oneDayButton.setEnabled(enabled);
    	}
    }
}
