package com.isti.traceview.data;

import java.io.Serializable;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.isti.traceview.common.TimeInterval;

/**
 * Socket data source
 * 
 * @author Max Kokoulin
 */
public abstract class SourceSocket extends Observable implements ISource, Serializable {
	private static Logger lg = Logger.getLogger(SourceSocket.class);
	private String name = null;
	private boolean parsed = false;
	private boolean enabled = true;
	private Integer timeout = 2000;
	private String server;
	private int port;
	private TimeZone timeZone;

	public SourceType getSourceType() {
		return SourceType.SOCKET;
	}

	public abstract FormatType getFormatType();

	public abstract Set<IChannel> parse(DataModule dataModule); 
	
	public abstract void load(Segment segment);
	
	public abstract List<Segment> getRawData(RawDataProvider rdp, TimeInterval ti);
	
	public abstract List<Segment> getRawData(RawDataProvider rdp);
	
	public abstract void deleteRawData(RawDataProvider rdp, TimeInterval ti);
	
	public synchronized boolean isParsed() {
		return parsed;
	}

	protected synchronized void setParsed(boolean parsed) {
		this.parsed = parsed;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the timeout
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the timeZone
	 */
	public TimeZone getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public String toString() {
		return getName() + ";" + getServer() + ":" + getPort() + (getTimeout()==null?"":(":"+getTimeout())) + ":" + getTimeZone().getID() + " enabled:" + isEnabled();
	}
	
	public void setChanged(){
		super.setChanged();
	}
	
	public void clearChanged(){
		super.clearChanged();
	}
}
