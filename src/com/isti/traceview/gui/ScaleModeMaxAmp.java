package com.isti.traceview.gui;

import java.util.List;

import com.isti.traceview.common.TimeInterval;
import com.isti.traceview.data.PlotData;

/**
 * State pattern realization for scale mode, Maximum Amplitude scaling. It means that value axis in each channel view scales to
 * to maximum and minimum values of this trace on the visible time range. Multiplier is the different for channels loaded in the slot.
 * 
 * @author Max Kokoulin
 */
public class ScaleModeMaxAmp extends ScaleModeAbstract implements IScaleModeState {
	List<PlotData> graphs = null;

	@Override
	public void init(List<PlotData> graphs, int currentGraphNum, List<Slot> allViews, TimeInterval timeRange, IMeanState meanState, int height) {
		maxValue = Double.NEGATIVE_INFINITY;
		double minValue = Double.POSITIVE_INFINITY;
		this.graphs = graphs;
		if (graphs.size()>currentGraphNum) {
			PlotData data = graphs.get(currentGraphNum);
			maxValue = meanState.getValue(data.getMaxValue(), data.getMeanValue());
			minValue = meanState.getValue(data.getMinValue(), data.getMeanValue());
		}
		if (maxValue == minValue || maxValue == Double.NEGATIVE_INFINITY || minValue == Double.POSITIVE_INFINITY) {
			amp = 100.0;
		} else {
			amp = maxValue - minValue;
		}
		this.height = height;
	}
	
	public int getY(double value, int graphNum) {
		double maxThis = graphs.get(graphNum).getMaxValue();
		double minThis = graphs.get(graphNum).getMinValue();
		double ampThis = maxThis-minThis;
		double newVal = (amp/ampThis)*value+maxValue-maxThis*(amp/ampThis);
		return super.getY(newVal, graphNum);
	}

	@Override
	public String getStateName() {
		return "MAXAMP";
	}
}
