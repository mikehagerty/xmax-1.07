package com.isti.traceview.gui;

import java.util.List;

import com.isti.traceview.data.IChannel;
import com.isti.traceview.data.PlotDataProvider;

/**
 * Default channel view factory, generates ordinary {@link Slot}
 * 
 * @author Max Kokoulin
 */
public class DefaultSlotFactory implements ISlotFactory {
	
	public int getInfoAreaWidth(){
		return 80;
	}

	public Slot getSlot(List<? extends IChannel> channels) {
		return new Slot(channels);
	}

	public Slot getSlot(IChannel channel) {
		return new Slot(channel);
	}

}
