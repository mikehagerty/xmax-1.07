package com.isti.traceview.gui;

import java.util.List;

import com.isti.traceview.common.TimeInterval;
import com.isti.traceview.data.PlotData;

/**
 * State pattern realization for scale mode. Scale mode defines method of trace scaling inside each
 * Slot
 */
public interface IScaleModeState {
	/**
	 * Should be called before other methods using
	 * 
	 * @param graphs
	 *            List<PlotData> for current Slot
	 * @param allViews
	 *            List<Slot> All views for graph panel
	 * @param timeRange
	 *            TimeInterval set in graph panel
	 * @param meanState
	 *            current mean state
	 * @param height
	 *            height of current view, in pixels
	 */
	public void init(List<PlotData> graphs, int currentGraphNum, List<Slot> allViews, TimeInterval timeRange, IMeanState meanState, int height);

	/**
	 * @param value
	 *            trace value
	 * @return Y screen panel coordinate to draw
	 */
	public int getY(double value, int graphNum);

	/**
	 * @param y
	 *            Y screen panel coordinate
	 * @return trace value
	 */
	public double getValue(int y);

	/**
	 * @return name of state
	 */
	public String getStateName();

	/**
	 * @return max value for all traces in current Slot
	 */
	public double getMaxValue();

	/**
	 * @return min value for all traces in current Slot
	 */
	public double getMinValue();
}
