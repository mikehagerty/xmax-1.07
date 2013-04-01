package com.isti.xmax.gui;

import java.util.List;


import com.isti.traceview.data.IChannel;
import com.isti.traceview.gui.ISlotFactory;

/**
 * Factory for customized {@link com.isti.traceview.Slot}s
 * 
 * @author Max Kokoulin
 */
public class XMAXSlotFactory implements ISlotFactory {
	
	public int getInfoAreaWidth(){
		return 80;
	}

	public XMAXSlot getSlot(List<? extends IChannel> channels) {
		return new XMAXSlot(channels, getInfoAreaWidth(), true);
	}

	public XMAXSlot getSlot(IChannel channel) {
		return new XMAXSlot(channel, getInfoAreaWidth(), true);
	}

}
