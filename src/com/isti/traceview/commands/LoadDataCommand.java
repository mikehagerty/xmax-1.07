package com.isti.traceview.commands;

import java.util.List;
import org.apache.log4j.Logger;

import com.isti.traceview.AbstractCommand;
import com.isti.traceview.common.TimeInterval;
import com.isti.traceview.data.IChannel;

/**
 * This command loads raw data into data providers from sources discovered on the parse stage
 * 
 * @author Max Kokoulin
 */

public class LoadDataCommand extends AbstractCommand {
	private static Logger lg = Logger.getLogger(LoadDataCommand.class);

	List<? extends IChannel> channels;
	TimeInterval ti = null;
	
	/**
	 * 
	 * @param channels list of data providers
	 * @param ti time interval to load
	 */
	public LoadDataCommand(List<? extends IChannel> channels, TimeInterval ti) {
		this.channels = channels;
		this.ti = ti;
		this.setPriority(5);
	}

	public void run() {
		try {
			super.run();
			for (IChannel channel: channels) {
				if (!channel.isLoadingStarted()) {
					lg.debug("Load data command: " + channel.toString() + ti);
					channel.load(ti);
					//lg.debug("Max Val = " + channel.getMaxValue() + ", Min Val = " + channel.getMinValue());
				}
			}
		} catch (Exception e) {
			lg.error("LoadDataCommand error: " + e.getMessage());
		}
	}
}
