package com.isti.traceview.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;

import com.isti.traceview.CommandExecutor;
import com.isti.traceview.TraceView;
import com.isti.traceview.TraceViewException;
import com.isti.traceview.commands.LoadDataCommand;
import com.isti.traceview.common.IEvent;
import com.isti.traceview.common.TimeInterval;
import com.isti.traceview.data.EventWrapper;
import com.isti.traceview.data.IChannel;
import com.isti.traceview.data.PlotData;
import com.isti.traceview.data.PlotDataPoint;
import com.isti.traceview.data.Segment;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.swing.JCheckBox;

import org.apache.log4j.Logger;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.RangeType;
import org.jfree.ui.RectangleEdge;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Graphics panel to plot several traces in the same time and values coordinate axis on a single panel. Has auxiliary panel on the left side and big graph panel to plot data on the
 * right side.
 * 
 * @author Max Kokoulin
 */

public class Slot extends JPanel implements Comparable, Observer {
	private static Logger lg = Logger.getLogger(Slot.class); // @jve:decl-index=0:

	public static boolean tooltipVisible = false;
	public static final int defaultInfoPanelWidth = 80;
	protected static int currentSelectionNumber = 0;
	protected static NumberFormat labelFormat = null;

	/**
	 * @uml.property name="plotDataProviders" multiplicity="(0 -1)" dimension="1"
	 */
	private List<IChannel> plotDataProviders = null; // @jve:decl-index=0:
	private List<PlotData> graphs = null;
	int height = 0;
	int maxValueAllChannels = Integer.MIN_VALUE;
	int minValueAllChannels = Integer.MAX_VALUE;
	double meanValue = Double.POSITIVE_INFINITY;

	private InfoPanel infoPanel = null;
	private GraphAreaPanel graphAreaPanel = null;
	private GraphPanel graphPanel = null;

	private int mousePressX;
	private int mousePressY;
	private NumberAxis axis = null; // @jve:decl-index=0:

	private int lastClickedY = Integer.MIN_VALUE; // to
	private int lastClickedX = Integer.MIN_VALUE;

	private List<MarkPosition> markPositions = null;
	private int selectionNumber = 0;
	private boolean isDrawSelectionCheckBox = true;
	private String unitName = "";
	protected int currentGraphNum = 0;

	/**
	 * Mouse adapter for GraphAreaPanel - internal panel containing graphs
	 */
	private IMouseAdapter mouseAdapter = null;

	static {
		labelFormat = NumberFormat.getInstance(Locale.getDefault());
		if (labelFormat instanceof DecimalFormat) {
			DecimalFormat df = (DecimalFormat) labelFormat;
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(2);
		}
	}

	public Slot(List<? extends IChannel> channels, int infoPanelWidth, boolean isDrawSelectionCheckBox) {
		super();
		String names = "";
		for (IChannel channel : channels) {
			names = names + channel.toString() + ";";
		}
		lg.debug("Slot created for list: " + names);
		initialize(infoPanelWidth, isDrawSelectionCheckBox);
		setPlotDataProviders(channels);
	}

	public Slot(IChannel channel, int infoPanelWidth, boolean isDrawSelectionCheckBox) {
		super();
		lg.debug("Slot created for " + channel.toString());
		initialize(infoPanelWidth, isDrawSelectionCheckBox);
		List<IChannel> lst = new ArrayList<IChannel>();
		lst.add(channel);
		setPlotDataProviders(lst);
	}

	public Slot(List<? extends IChannel> channels) {
		this(channels, defaultInfoPanelWidth, true);
	}

	public Slot(IChannel channel) {
		this(channel, defaultInfoPanelWidth, true);
	}

	public Slot() {
		this(new ArrayList<IChannel>(), defaultInfoPanelWidth, true);
	}

	private void initialize(int infoPanelWidth, boolean isDrawSelectionCheckBox) {
		ToolTipManager.sharedInstance().unregisterComponent(this);
		axis = new NumberAxis();
		axis.setRangeType(RangeType.FULL);
		this.setLayout(new BorderLayout());
		this.add(getInfoPanel(infoPanelWidth, isDrawSelectionCheckBox), BorderLayout.WEST);
		this.add(getGraphAreaPanel(), BorderLayout.CENTER);
		markPositions = new ArrayList<MarkPosition>();
	}

	/**
	 * Sets mouse adapter defines mouse action inside this Slot
	 * 
	 * @param ma
	 */
	public void setMouseAdapter(IMouseAdapter ma) {
		mouseAdapter = ma;
	}

	/**
	 * Gets current mouse adapter defines mouse action inside this Slot
	 */
	public IMouseAdapter getMouseAdapter() {
		return mouseAdapter;
	}

	/**
	 * @return value coordinate of last clicked point on this Slot (in pixels inside graph area panel)
	 */
	public int getLastClickedY() {
		return lastClickedY;
	}

	/**
	 * @return time coordinate of last clicked point on this Slot (in pixels inside graph area panel)
	 */
	public int getLastClickedX() {
		return lastClickedX;
	}

	/**
	 * @return width of area graph itself occupy, without left auxiliary panel
	 */
	public int getGraphAreaWidth() {
		return graphAreaPanel.width;
	}

	/**
	 * @param x
	 *            time coordinate of point (in pixels inside graph area panel)
	 * @return Set of events in this point
	 */
	public Set<IEvent> getEvents(int x) {
		Set<EventWrapper> eventWrappers = graphAreaPanel.getEvents(x);
		if (eventWrappers == null)
			return null;
		Set<IEvent> ret = new HashSet<IEvent>();
		for (EventWrapper eventWrapper : eventWrappers) {
			ret.add(eventWrapper.getEvent());
		}
		return ret;
	}

	/**
	 * @return Necessary count of data point in graph to draw inside this panel
	 */
	public int getPointCount() {
		return graphAreaPanel.getWidth();
	}

	public void update(Observable observable, Object arg) {
		lg.debug(this + ": update request from " + observable);
		if (arg instanceof TimeInterval) {
			TimeInterval ti = (TimeInterval) arg;
			lg.debug(this + " updating for range " + ti + " due to request from " + observable.getClass().getName());
		}
		updateData();
		graphAreaPanel.repaint();
		getGraphPanel().setChanged();
		getGraphPanel().notifyObservers();
	}

	/**
	 * Sets graph panel contains this Slot
	 * 
	 * @param gp
	 */
	public void setGraphPanel(GraphPanel gp) {
		this.graphPanel = gp;
	}

	/**
	 * Gets graph panel contains this Slot
	 */
	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * @return time coordinate of last pressed point on this Slot (in pixels inside graph area panel)
	 */
	public int getMousePressX() {
		return mousePressX;
	}

	/**
	 * @return value coordinate of last pressed point on this Slot (in pixels inside graph area panel)
	 */
	public int getMousePressY() {
		return mousePressY;
	}

	/**
	 * Adds marker to screen. Marker image loaded from markPosition.gif file placed in the jar. Parameters are coordinates of image's center.
	 * 
	 * @param time
	 *            time position (in internal Java time representation)
	 * @param value
	 *            value position
	 */
	public void addMarkPosition(long time, double value) {
		markPositions.add(new MarkPosition(time, value));
	}

	/**
	 * Clears all markers
	 */
	public void clearMarkPositions() {
		markPositions.clear();
	}

	/**
	 * Getter of the property <tt>plotDataProviders</tt>
	 * 
	 * @return the list of PlotDataProviders drawn in this Slot.
	 * @uml.property name="plotDataProviders"
	 */
	public List<? extends IChannel> getPlotDataProviders() {
		return plotDataProviders;
	}

	/**
	 * @return pixelized graph data drawn in this Slot.
	 */
	public List<PlotData> getPlotData() {
		return graphs;
	}

	/**
	 * Setter of the property <tt>plotDataProviders</tt>
	 * 
	 * @param channels
	 *            List of plotDataProviders to draw inside this Slot.
	 * @uml.property name="plotDataProviders"
	 */
	public void setPlotDataProviders(List<? extends IChannel> channels) {
		if (plotDataProviders == null) {
			plotDataProviders = new ArrayList<IChannel>();
		} else {
			for (IChannel channel : plotDataProviders) {
				channel.deleteObserver(this);
			}
			plotDataProviders.clear();
		}

		for (IChannel channel : channels) {
			plotDataProviders.add(channel);
			channel.addObserver(this);
			lg.debug("Observer for " + channel.toString() + " added");
			if (channel.getMaxValue() > maxValueAllChannels) {
				maxValueAllChannels = channel.getMaxValue();
			}
			if (channel.getMinValue() < minValueAllChannels) {
				minValueAllChannels = channel.getMinValue();
			}
		}
		CommandExecutor.getInstance().execute(new LoadDataCommand(channels, null));
		lastClickedY = Integer.MIN_VALUE;
		refreshUnits();
	}

	public void addPlotDataProvider(IChannel channel) {
		channel.deleteObserver(this);
		channel.addObserver(this);
		lg.debug("Observer for " + channel.toString() + " added");
		plotDataProviders.add(channel);
		if (channel.getMaxValue() > maxValueAllChannels) {
			maxValueAllChannels = channel.getMaxValue();
		}
		if (channel.getMinValue() < minValueAllChannels) {
			minValueAllChannels = channel.getMinValue();
		}
		List<IChannel> channels = new ArrayList<IChannel>();
		channels.add(channel);
		CommandExecutor.getInstance().execute(new LoadDataCommand(channels, null));
		lastClickedY = Integer.MIN_VALUE;
		refreshUnits();
	}

	public void removePlotDataProvider(IChannel channel) {
		channel.deleteObserver(this);
		plotDataProviders.remove(channel);
		refreshUnits();
	}

	public void removeAllPlotDataProviders() {
		Iterator<? extends IChannel> it = plotDataProviders.iterator();
		while (it.hasNext()) {
			IChannel pdp = it.next();
			pdp.deleteObserver(this);
			it.remove();
		}
		refreshUnits();
	}

	/**
	 * Return time interval of loaded data
	 */
	public TimeInterval getLoadedTimeRange() {
		TimeInterval ret = null;
		for (IChannel channel : plotDataProviders) {
			if (ret == null) {
				ret = channel.getTimeRange();
			} else {
				ret = TimeInterval.getAggregate(ret, channel.getTimeRange());
			}
		}
		return ret;
	}

	/**
	 * Prepares pixelized data for PlotDataProviders to draw. Should be called before paint.
	 */
	public synchronized void updateData() {
		if (graphs == null) {
			graphs = Collections.synchronizedList(new ArrayList<PlotData>());
		}
		synchronized (graphs) {
			graphs.clear();
			int width = graphAreaPanel.getWidth();
			lg.debug("Updating data " + this + "Width = " + width + ", count providers=" + plotDataProviders.size());
			for (IChannel channel : plotDataProviders) {
				if (!isChannelAlreadyProcessed(channel)) {
					lg.debug("processing channel: " + channel);
					PlotData data = null;
					try {
						data = channel.getPlotData(graphPanel.getTimeRange(), width, graphPanel.getRotation(), graphPanel.getFilter(), graphPanel.getColorMode());
					} catch (TraceViewException e) {
						graphPanel.setRotation(null);
						JOptionPane.showMessageDialog(TraceView.getFrame(), e, "Rotation warning", JOptionPane.WARNING_MESSAGE);
						try {
							data = channel.getPlotData(graphPanel.getTimeRange(), width, null, graphPanel.getFilter(), graphPanel.getColorMode());
						} catch (TraceViewException e1) {
							// do nothing
						}
					}
					if (!data.ignoreFlag) {
						lg.debug("Added " + data);
						graphs.add(data);
						meanValue = data.getMeanValue();
					}
				}
			}
			Collections.sort(graphs);
			lg.debug("Updating data end: " + this + "Width = " + width + ", count providers=" + plotDataProviders.size() + ", count graphs=" + graphs.size());
		}
	}

	private void refreshUnits() {
		List<String> units = new ArrayList<String>();
		for (IChannel channel : getPlotDataProviders()) {
			String u = channel.getUnitName();
			if (!units.contains(u)) {
				units.add(u);
			}
		}
		unitName = "";
		for (String u : units) {
			if (unitName.length() == 0) {
				unitName = u;
			} else {
				unitName += ("," + u);
			}
		}
	}

	private boolean isChannelAlreadyProcessed(IChannel channel) {
		synchronized (graphs) {
			for (PlotData pd : getPlotData()) {
				if (pd.getLabel().equals(channel.getName()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Cistomized method to paint events.
	 * 
	 * @param x
	 *            - horizontal number of pixel
	 * @param ymax
	 *            - vertical pixel of upper trace bar end in this point
	 * @param ymax
	 *            - vertical pixel of bottom trace bar end in this point
	 */
	public void paintCustomEvent(Graphics g, EventWrapper eventWrapper, int x, int ymax, int ymin) {

	}

	/**
	 * This method initializes infoPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private InfoPanel getInfoPanel(int infoPanelWidth, boolean isDrawSelectionCheckBox) {
		if (infoPanel == null) {
			infoPanel = new InfoPanel(this, infoPanelWidth, isDrawSelectionCheckBox);
		}
		return infoPanel;
	}

	/**
	 * This method initializes graphAreaPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	protected GraphAreaPanel getGraphAreaPanel() {
		if (graphAreaPanel == null) {
			graphAreaPanel = new GraphAreaPanel(this);
		}
		return graphAreaPanel;
	}

	/**
	 * Gets string representation of Slot in the debug purposes
	 */
	public String toString() {
		String ret = "";
		for (IChannel channel : plotDataProviders) {
			ret = ret + channel.getStation().getName() + "/" + channel.getChannelName() + " ";
		}
		return "Slot: " + ret;
	}

	/**
	 * @return flag if this Slot selected(by checkbox on info panel)
	 */
	public boolean isSelected() {
		return infoPanel.isSelected();
	}

	/**
	 * Adds MouseListener to internal graph panel
	 */
	public void addMouseListener(MouseListener l) {
		graphAreaPanel.addMouseListener(l);
	}

	/**
	 * Adds MouseMotionListener to internal graph panel
	 */
	public void addMouseMotionListener(MouseMotionListener l) {
		graphAreaPanel.addMouseMotionListener(l);
	}

	/**
	 * Sets cursor for internal graph panel
	 */
	public void setCursor(Cursor cursor) {
		graphAreaPanel.setCursor(cursor);
	}

	/**
	 * When user selects several Slots on the {@link GraphPanel}, each Slot stores his sequential number in this selection. If GraphPanel will be set to Selection mode, Slot will
	 * be shown in the order of their selectionNumbers
	 * 
	 * @return sequential number during selection
	 */
	public int getSelectionNumber() {
		return selectionNumber;
	}

	/**
	 * This method should be called after selection mode changing in GraphPanel
	 */
	public void selectionPerformed() {

	}

	/**
	 * This method should be overloaded to paint something over graph line.
	 * 
	 * @param g
	 */
	public void paintForeground(Graphics g) {

	}

	/**
	 * This method should be overloaded to paint something graph line.
	 * 
	 * @param g
	 */
	public void paintBackground(Graphics g) {

	}

	/**
	 * Comparator by selection number
	 */
	public int compareTo(Object o) {
		if (o instanceof Slot) {
			Slot c = (Slot) o;
			return new Integer(getSelectionNumber()).compareTo(new Integer(c.getSelectionNumber()));
		} else {
			return 1;
		}
	}

	public void setGraphAreaColor(Color color) {
		getGraphAreaPanel().setBackground(color);
	}

	public void setGraphAreaBorder(Border border) {
		getGraphAreaPanel().setBorder(border);
	}

	public void setInfoAreaColor(Color color) {
		infoPanel.setBackground(color);
	}

	public void setInfoAreaBorder(Border border) {
		infoPanel.setBorder(border);
	}

	public void setDrawYaxis(boolean drawYaxis) {
		infoPanel.setDrawAxis(drawYaxis);
	}

	protected void initScale() {
		graphPanel.getScaleMode().init(getPlotData(), currentGraphNum,
				(graphPanel.getOverlayState() == true) || (graphPanel.getSelectState() == true) ? graphPanel.getCurrentSlots() : graphPanel.getSlots(), graphPanel.getTimeRange(),
				graphPanel.getMeanState(), graphAreaPanel.getHeight());
		// lg.debug("scaleMode Initialized:" + scaleMode.getStateName()
		// +
		// scaleMode.getMaxValue() + scaleMode.getMinValue());
	}

	/**
	 * Left panel for auxiliary information: selection checkbox, axis painting etc
	 */
	class InfoPanel extends JPanel implements ItemListener, MouseListener {
		private NonPrintableCheckBox selected = null;
		String max = "";
		String min = "";
		boolean drawAxis = true;
		Font labelsFont = null;
		Font rangeFont = null;;

		public InfoPanel(Slot cv, int infoPanelWidth, boolean isDrawSelectionCheckBox) {
			super();
			setBackground(Slot.this.getBackground());
			setPreferredSize(new Dimension(infoPanelWidth, 0));
			setBorder(BorderFactory.createEtchedBorder());
			setLayout(null);
			if (isDrawSelectionCheckBox) {
				getSelected().setBounds(1, 1, 25, 25);
				add(getSelected());
			}
			Font defaultFont = getFont();
			labelsFont = new Font(defaultFont.getName(), Font.BOLD, defaultFont.getSize());
			rangeFont = new Font(defaultFont.getName(), defaultFont.getStyle(), defaultFont.getSize() - 3);
			setDrawAxis(drawAxis);
			addMouseListener(this);
		}

		public void setRange(double min, double max) {
			if (!Double.isNaN(max) && !Double.isInfinite(max)) {
				this.max = labelFormat.format(max);
			} else {
				this.max = "";
			}
			if (!Double.isNaN(min) && !Double.isInfinite(min)) {
				this.min = labelFormat.format(min);
			} else {
				this.min = "";
			}
		}

		public boolean isSelected() {
			return selected.isSelected();
		}

		/**
		 * @return the drawAxis
		 */
		public boolean isDrawAxis() {
			return drawAxis;
		}

		/**
		 * @param drawAxis
		 *            the drawAxis to set
		 */
		public void setDrawAxis(boolean drawAxis) {
			this.drawAxis = drawAxis;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			int fontHeight = getFontMetrics(getFont()).getHeight();
			g.drawString(unitName, 3, getHeight() - fontHeight - 3);
			if (drawAxis) {
				axis.draw((Graphics2D) g, getWidth() - 5, new Rectangle(0, 0, getWidth(), getHeight()), new Rectangle(0, 0, getWidth(), getHeight()), RectangleEdge.LEFT, null);
			} else {
				//drawing channel labels
				g.setFont(labelsFont);
				fontHeight = getFontMetrics(labelsFont).getHeight();
				synchronized (graphs) {
					int i = 0;
					int size = getPlotData().size();
					for (PlotData data : getPlotData()) {
						int y = ((getHeight() - size * fontHeight) + fontHeight) / 2;
						g.setColor(data.getLabelColor());
						g.drawString(data.getLabel(), 27, y + (i++ * fontHeight));
					}
				}
				//drawing max and min axis values
				g.setFont(rangeFont);
				if (graphPanel.getScaleMode() instanceof ScaleModeMaxAmp && graphs.size() > currentGraphNum) {
					g.setColor(graphs.get(currentGraphNum).getTraceColor());
				} else {
					g.setColor(Color.BLACK);
				}
				fontHeight = getFontMetrics(rangeFont).getHeight();
				int fontWidth = getFontMetrics(rangeFont).stringWidth(max);
				g.drawString(max, getWidth() - fontWidth - 3, fontHeight);
				fontWidth = getFontMetrics(rangeFont).stringWidth(min);
				g.drawString(min, getWidth() - fontWidth - 3, getHeight() - 4);
			}
		}

		/**
		 * This method initializes selected
		 * 
		 * @return Customized checkbox for Slot selection which drawn on screen and don't drawn during rendering for print
		 */
		private NonPrintableCheckBox getSelected() {
			if (selected == null) {
				selected = new NonPrintableCheckBox();
				selected.addItemListener(this);
			}
			return selected;
		}

		@Override
		public void print(Graphics g) {
			Color orig = getBackground();
			setBackground(Color.WHITE);

			// wrap in try/finally so that we always restore the state
			try {
				super.print(g);
			} finally {
				setBackground(orig);
			}
		}

		//------------From InputListener-------------------------------------------------------

		/** Listens to the check box. */
		public void itemStateChanged(ItemEvent e) {
			if (!(graphPanel.getSelectState() || graphPanel.getOverlayState())) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					selectionNumber = 0;
					graphPanel.getSelectedSlots().remove(Slot.this);
				} else if (e.getStateChange() == ItemEvent.SELECTED) {
					graphPanel.getSelectedSlots().add(Slot.this);
					currentSelectionNumber++;
					selectionNumber = currentSelectionNumber;
				}
			}
			selectionPerformed();
		}

		//--------------From MouseListener-----------------------------------------------------
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && (graphPanel.getScaleMode() instanceof ScaleModeMaxAmp)) {
				int fontHeight = getFontMetrics(labelsFont).getHeight();
				if (e.getX() > 25) {
					synchronized (graphs) {
						int i = 0;
						int y = ((getHeight() - getPlotData().size() * fontHeight) + fontHeight) / 2;
						for (PlotData data : getPlotData()) {
							if(e.getY()>y-fontHeight && e.getY()<y){
								currentGraphNum = i;
								graphPanel.forceRepaint();
								break;
							}
							y+=fontHeight;
							i++;
						}
					}
				}
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		//-------------------------------------------------------------------------------------

		private class NonPrintableCheckBox extends JCheckBox {
			public NonPrintableCheckBox() {
				super();
				setPreferredSize(new Dimension(20, 20));
				setSize(new Dimension(20, 20));
				setMinimumSize(new Dimension(20, 20));
				setMaximumSize(new Dimension(20, 20));
			}

			public void paint(Graphics g) {
				if (!isPaintingForPrint()) { // works only in jre 1.6
					super.paint(g);
				}
			}
		}
	}

	/**
	 * Big right panel for graphs drawing
	 */
	public class GraphAreaPanel extends JPanel implements MouseInputListener {
		/**
		 * Panel height in pixel
		 */
		int height;

		/**
		 * Panel width in pixel
		 */
		int width;

		/**
		 * Apmlitude of contained graph - precomputed value to increase speed of paintComponent() calculations
		 */
		int amp;

		Slot cv = null;

		private int button = MouseEvent.NOBUTTON;
		private int fontHeight = 0;

		public GraphAreaPanel(Slot cv) {
			super();
			this.cv = cv;
			setBackground(Slot.this.getBackground());
			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createEtchedBorder());
			addMouseListener(this);
			addMouseMotionListener(this);
			setToolTipText("YYY");
		}

		public void paint(Graphics g) {
			// lg.debug("Repainting " + this);
			super.paint(g);
			// maxValue = Integer.MIN_VALUE;
			// minValue = Integer.MAX_VALUE;
			height = this.getHeight();
			width = this.getWidth();
			IScaleModeState scaleMode = graphPanel.getScaleMode();
			IMeanState meanState = graphPanel.getMeanState();
			IOffsetState offsetState = graphPanel.getOffsetState();
			if (getPlotData() != null) {
				initScale();
				paintBackground(g);
				if (scaleMode.getMinValue() != Double.POSITIVE_INFINITY && scaleMode.getMaxValue() != Double.NEGATIVE_INFINITY) {
					axis.setRange(scaleMode.getMinValue(), scaleMode.getMaxValue());
				}
				infoPanel.setRange(scaleMode.getMinValue(), scaleMode.getMaxValue());
				// Offset step is 1/20 of graph height
				offsetState.setShift((scaleMode.getMaxValue() - scaleMode.getMinValue()) / 20);
				// lg.debug("Set Slot " + this + " boundaries: " + scaleMode.getMaxValue() + "-" + scaleMode.getMinValue());
				// Graph's number, used to separate graphs then overlay mode is activated
				int graphNum = 0;
				Color segmentColor = null;
				synchronized (graphs) {
					for (PlotData data : getPlotData()) {
						int i = 0;
						// lg.debug("Drawing graph " + i + ", " + data);
						// strokes for previous pixel
						List<Stroke> yprev = new ArrayList<Stroke>();
						for (PlotDataPoint[] points : data.getPixels()) {
							int j = 0;
							double mean = data.getMeanValue();
							for (PlotDataPoint point : points) {
								// add previous stroke to list if list has unsuffisient length
								if (yprev.size() == j || yprev.get(j) == null) {
									yprev.add(j, new Stroke());
								}
								Stroke current = new Stroke();
								if (point.getSegmentNumber() >= 0) {
									segmentColor = graphPanel.getColorMode().getSegmentColor(graphNum + point.getSegmentNumber(), graphNum + point.getRawDataProviderNumber(),
											graphNum + point.getContinueAreaNumber(), data.getTraceColor());
									if (point.getSegmentNumber() == 0 && data.getLabelColor() == null) {
										data.setLabelColor(segmentColor);
									}
									g.setColor(segmentColor);
									// reinit previous stroke if color differs
									if ((yprev.get(j).color != null) && (yprev.get(j).color != segmentColor)) {
										yprev.set(j, new Stroke());
									}
									current.color = segmentColor;
									current.top = scaleMode.getY(meanState.getValue(offsetState.getValue(point.getTop(), point.getSegmentNumber()), mean), graphNum);
									current.bottom = scaleMode.getY(meanState.getValue(offsetState.getValue(point.getBottom(), point.getSegmentNumber()), mean), graphNum);
									// lg.debug("Drawing pixel " + j + ": " + point.getTop() + "-" + point.getBottom() + ", " + current);
									g.drawLine(i, current.top, i, current.bottom);
									if (i > 0) {
										// fill vertical gaps
										if (current.bottom < yprev.get(j).top) {
											// lg.debug("Fill gap at top: " + yprev.get(j).top + "-" + current.bottom);
											g.drawLine(i - 1, yprev.get(j).top, i, current.bottom);
										}
										if (current.top > yprev.get(j).bottom) {
											// lg.debug("Fill gap at bottom: " + yprev.get(j).bottom + "-" + current.top);
											g.drawLine(i - 1, yprev.get(j).bottom, i, current.top);
										}
									}
									yprev.set(j, current);
								} else {
									// we have gap, set previous values to it's default
									yprev.set(j, new Stroke());
								}
								// drawing events
								long currentTime = getTime(i);
								for (EventWrapper eventWrapper : point.getEvents()) {
									lg.debug("drawing event front");
									g.setColor(eventWrapper.getEvent().getColor());
									if (eventWrapper.getEvent().getType().equals("ARRIVAL") && graphPanel.getPhaseState()) {
										// drawing phases
										if (graphPanel.getSelectedEarthquakes().contains(eventWrapper.getEvent().getParameterValue("EARTHQUAKE"))
												&& graphPanel.getSelectedPhases().contains(eventWrapper.getEvent().getParameterValue("PHASE"))) {
											g.drawLine(i, getHeight(), i, 0);
											g.drawString((String) eventWrapper.getEvent().getParameterValue("PHASE"), i + 2, getHeight() - 5);
										}
									} else if (eventWrapper.getEvent().getType().equals("PICK") && graphPanel.getPickState()) {
										// drawing picks
										g.drawLine(i, getHeight(), i, 0);
										Polygon p = new Polygon();
										p.addPoint(i, 0);
										p.addPoint(i + 4, 4);
										p.addPoint(i, 8);
										g.fillPolygon(p);
									} else {
										paintCustomEvent(g, eventWrapper, i, current.top, current.bottom);
										g.setColor(segmentColor);
									}
								}
								j++;
							}
							while (j < yprev.size()) {
								yprev.set(j, new Stroke());
								j++;
							}
							i++;
						}
						graphNum++;
					}
				}
				paintForeground(g);
				if (plotDataProviders != null) {
					// drawing marks
					for (MarkPosition mp : markPositions) {
						Image image = graphPanel.getMarkPositionImage();
						g.drawImage(image, graphPanel.getXposition(mp.getTime()) - image.getHeight(this) / 2, graphPanel.getScaleMode().getY(mp.getValue(), currentGraphNum)
								- image.getHeight(this) / 2, this);
					}
				}
			} else {
				infoPanel.setRange(Double.NaN, Double.NaN);
			}
			infoPanel.repaint();
			// lg.debug("Repainting end " + this);
		}

		/**
		 * Computes trace time value
		 * 
		 * @param x
		 *            screen panel coordinate
		 * @return time value in internal Java format
		 */
		public long getTime(int x) {
			// lg.debug("Slot getTime: " + x);
			TimeInterval ti = graphPanel.getTimeRange();
			return new Double(ti.getStart() + x * new Double(ti.getDuration()) / getWidth()).longValue();
		}

		public Slot getOwner() {
			return cv;
		}

		public void mouseMoved(MouseEvent e) {
			int x = e.getX();
			if ((button != MouseEvent.NOBUTTON) && (e.isControlDown() || e.isShiftDown())) {
				mouseDragged(e);
			} else {
				if (mouseAdapter != null) {
					mouseAdapter.mouseMoved(x, e.getY(), e.getModifiers(), cv);
				}
				MouseEvent converted = SwingUtilities.convertMouseEvent(this, e, graphPanel);
				graphPanel.dispatchEvent(new MouseEvent(graphPanel, converted.getID(), e.getWhen(), converted.getModifiers(), converted.getX(), converted.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));

			}
		}

		public void mouseDragged(MouseEvent e) {
			// lg.debug("Slot.mouseDragged");

			if (mouseAdapter != null) {
				mouseAdapter.mouseDragged(e.getX(), e.getY(), e.getModifiers(), cv);
			}
			MouseEvent converted = SwingUtilities.convertMouseEvent(this, e, graphPanel);
			graphPanel.dispatchEvent(new MouseEvent(graphPanel, converted.getID(), e.getWhen(), converted.getModifiers(), converted.getX(), converted.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));

		}

		public void mouseClicked(MouseEvent e) {
			int clickedX = e.getX();
			int clickedY = e.getY();
			long clickedTime = graphPanel.getTime(clickedX);
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (mouseAdapter != null) {
					mouseAdapter.mouseClickedButton1(clickedX, clickedY, e.getClickCount(), e.getModifiers(), cv);
				}
				lastClickedY = clickedY;
				lastClickedX = clickedX;
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				if (mouseAdapter != null) {
					mouseAdapter.mouseClickedButton3(clickedX, clickedY, e.getClickCount(), e.getModifiers(), cv);
				}
			}
			MouseEvent converted = SwingUtilities.convertMouseEvent(this, e, graphPanel);
			graphPanel.dispatchEvent(new MouseEvent(graphPanel, converted.getID(), e.getWhen(), converted.getModifiers(), converted.getX(), converted.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));

		}

		public void mouseEntered(MouseEvent e) {
			mouseAdapter.mouseEntered(e.getX(), e.getY(), e.getModifiers(), cv);

		}

		public void mouseExited(MouseEvent e) {
			mouseAdapter.mouseExited(e.getX(), e.getY(), e.getModifiers(), cv);
		}

		public void mousePressed(MouseEvent e) {
			// lg.debug("Slot.mousePressed");
			mousePressX = e.getX();
			mousePressY = e.getY();
			graphPanel.getScaleMode().init(getPlotData(), currentGraphNum,
					(graphPanel.getOverlayState() == true) || (graphPanel.getSelectState() == true) ? graphPanel.getCurrentSlots() : graphPanel.getSlots(),
					graphPanel.getTimeRange(), graphPanel.getMeanState(), getHeight());
			// one-button mouse Mac OSX behaviour emulation
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.isShiftDown()) {
					button = MouseEvent.BUTTON2;
				} else if (e.isControlDown()) {
					button = MouseEvent.BUTTON3;
				} else {
					button = MouseEvent.BUTTON1;
				}
			} else {
				button = e.getButton();
			}
			MouseEvent converted = SwingUtilities.convertMouseEvent(this, e, graphPanel);
			graphPanel.dispatchEvent(new MouseEvent(graphPanel, converted.getID(), e.getWhen(), converted.getModifiers(), converted.getX(), converted.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));
		}

		public void mouseReleased(MouseEvent e) {
			// lg.debug("Slot.mouseReleased");
			if (button != MouseEvent.NOBUTTON && ((mousePressX != e.getX()) || (mousePressY != e.getY()))) {
				if (button == MouseEvent.BUTTON3 || (button == MouseEvent.BUTTON1 && e.isControlDown() == true)) {
					if (mouseAdapter != null) {
						mouseAdapter.mouseReleasedButton3(e.getX(), e.getY(), e.getModifiers(), cv);
					}
				} else if (e.getButton() == MouseEvent.BUTTON1) {
					if (mouseAdapter != null) {
						mouseAdapter.mouseReleasedButton1(e.getX(), e.getY(), e.getModifiers(), cv);
					}
				}
			}
			button = MouseEvent.NOBUTTON;
			MouseEvent converted = SwingUtilities.convertMouseEvent(this, e, graphPanel);
			graphPanel.dispatchEvent(new MouseEvent(graphPanel, converted.getID(), e.getWhen(), converted.getModifiers(), converted.getX(), converted.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));

		}

		// Hack to correct repaint in all-screen mode
		private boolean toolTipTextWasChanged = false;

		public String getToolTipText(MouseEvent event) {
			int x = event.getX();
			int y = event.getY();
			int channelNumber = 0;
			// lg.debug("getToolTipText: X=" + x + "; Y=" + y);
			if (fontHeight != 0) {
				channelNumber = y / fontHeight;
			}
			if ((channelNumber < plotDataProviders.size()) && (x > getWidth() - 120) && (x < getWidth() - 20)) {
				IChannel channel = plotDataProviders.get(channelNumber);
				toolTipTextWasChanged = true;
				// lg.debug("getToolTipText: unitL");
				return getChannelLabelText(channel);
			} else if (graphPanel.getShowBlockHeader()) {
				toolTipTextWasChanged = true;
				IChannel channel = plotDataProviders.get(0);
				return getBlockHeaderText(channel, x);
			} else {
				// lg.debug("getToolTipText: empty");
				if (toolTipTextWasChanged) {
					toolTipTextWasChanged = false;
				}
				return null;
			}
		}

		protected String getChannelLabelText(IChannel channel) {
			String respname = "No response";
			try {
				if (channel.getResponse() != null) {
					respname = channel.getResponse().getFileName();
				}
			} catch (TraceViewException e) {
				// do nothing
			}
			return "<html>" + channel.getName() + "<br><i>Start time: </i> "
					+ TimeInterval.formatDate(channel.getTimeRange().getStartTime(), TimeInterval.DateFormatType.DATE_FORMAT_MIDDLE) + "<br><i>Duration: </i> "
					+ channel.getTimeRange().convert() + "<br><i>Sample rate: </i>" + channel.getSampleRate() + " ms <br>" + respname + "</html>";
		}

		protected String getBlockHeaderText(IChannel channel, int x) {
			long time = getTime(x);
			List<Segment> segments = channel.getRawData(new TimeInterval(time, time));
			if (segments.size() > 0) {
				return segments.get(0).getBlockHeaderText(time);
			} else {
				return "<html>There is no data in this place</html>";
			}
		}

		public JToolTip createToolTip() {
			return new CVToolTip();
		}

		class CVToolTip extends JToolTip {
			public CVToolTip() {
				lg.debug("CVToolTip: create");
				tooltipVisible = true;
			}

			public void show() {
				lg.debug("CVToolTip: show");
				super.show();
			}

			public void hide() {
				lg.debug("CVToolTip: hide");
				super.hide();
			}

			public void repaint() {
				super.repaint();
				// graphPanel.repaint();

			}

			protected void finalize() throws Throwable {
				lg.debug("CVToolTip: false");
				tooltipVisible = false;
				super.finalize();
			}

		}

		private Set<EventWrapper> getEvents(int x) {
			// lg.debug("getEvents: x= " + x);
			if (getPlotData() != null) {
				synchronized (graphs) {
					for (PlotData data : getPlotData()) {
						if (data.getPointCount() > x) {
							Set<EventWrapper> ret = new HashSet<EventWrapper>();
							for (PlotDataPoint dp : data.getPixels().get(x)) {
								if (dp.getEvents().size() != 0) {
									ret.addAll(dp.getEvents());
								}
							}
							if (ret.size() > 0) {
								return ret;
							} else {
								return null;
							}
						} else {
							return null;
						}
					}
				}
			}
			return null;
		}

		@Override
		public void print(Graphics g) {
			Color orig = getBackground();
			setBackground(Color.WHITE);

			// wrap in try/finally so that we always restore the state
			try {
				super.print(g);
			} finally {
				setBackground(orig);
			}
		}
	}

}// @jve:decl-index=0:visual-constraint="10,10"

/*
 * class CustomToolTipManager extends ToolTipManager { public CustomToolTipManager(){} public void mouseMoved(MouseEvent event){ super.mouseMoved(event); if(event.getSource()
 * instanceof Slot){ Slot cv = (Slot)event.getSource(); cv.getGraphPanel().forceRepaint = true; } } }
 */

class Stroke {
	int top = Integer.MIN_VALUE;
	int bottom = Integer.MAX_VALUE;
	Color color = null;

	public String toString() {
		return "Stroke: " + top + "-" + bottom + ", color " + color;
	}
}

/**
 * Class to represent marker on graph area.
 */
class MarkPosition {
	private long time = Long.MIN_VALUE;
	private double value = Double.NEGATIVE_INFINITY;

	public MarkPosition(long time, double value) {
		this.time = time;
		this.value = value;
	}

	public long getTime() {
		return time;
	}

	public double getValue() {
		return value;
	}
}
