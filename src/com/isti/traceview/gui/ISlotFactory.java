package com.isti.traceview.gui;

import java.util.List;

import com.isti.traceview.data.IChannel;

/**
 * Factory for {@link Slot}. Library users can create factory for their own, customized
 * Slots
 * 
 * @author Max Kokoulin
 */
public interface ISlotFactory {
	public int getInfoAreaWidth();
	
	public Slot getSlot(List<? extends IChannel> channels);

	public Slot getSlot(IChannel channel);

}
