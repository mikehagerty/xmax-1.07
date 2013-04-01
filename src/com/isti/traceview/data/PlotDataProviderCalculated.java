package com.isti.traceview.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

import com.isti.traceview.TraceViewException;
import com.isti.traceview.common.Station;
import com.isti.traceview.common.TimeInterval;

public abstract class PlotDataProviderCalculated extends PlotDataProvider {
	private static Logger lg = Logger.getLogger(PlotDataProviderCalculated.class);

	protected List<IChannel> sources;
	
	/**
	 * 
	 * @param toProcess list of data to integrate, the same begin and size
	 * @param sources list of channels data was clipped from, order corresponds the one in toProcess list
	 * @return
	 * @throws TraceViewException
	 */
	public abstract SegmentData calculateData(List<SegmentData> toProcess, int sequenceNumber, List<IChannel> sources) throws TraceViewException;

	private Segment calculateData(TimeInterval ti, int sequenceNumber) throws TraceViewException {
		List<SegmentData> toProcess = new ArrayList<SegmentData>();
		for (IChannel rdp : sources) {
			List<Segment> data = rdp.getRawData(ti);
			if (data!=null) {
				if (data.size() != 1) {
					throw new TraceViewException("PlotDataProviderCalculated.calculateData: segments list size not equals 1 :" + data.size() + " for " + rdp);
				}
				SegmentData toAdd = data.get(0).getData(ti);
				if (toProcess.size() > 0) {
					if (!(toAdd.startTime == toProcess.get(0).startTime)) {
						throw new TraceViewException("PlotDataProviderCalculated.calculateData: start times differ " + rdp);
					}
					if (!(toAdd.sampleRate == toProcess.get(0).sampleRate)) {
						throw new TraceViewException("PlotDataProviderCalculated.calculateData: sample rates differ " + rdp);
					}
					if (!(toAdd.data.length == toProcess.get(0).data.length)) {
						throw new TraceViewException("PlotDataProviderCalculated.calculateData: data length differ " + rdp);
					}
				}
				toProcess.add(toAdd);
			} else {
				toProcess.clear();
				break;
			}
		}
		return new Segment(null,calculateData(toProcess, sequenceNumber, sources));
	}

	public PlotDataProviderCalculated(String networkName, Station station, String channelName, String locationName, List<IChannel> sources) throws TraceViewException {
		super(channelName, station, networkName, locationName);
		usePixelCache=false;
		double sampleRate = 0.0;
		for (IChannel rdp : sources) {
			if ((sampleRate == 0.0) && rdp.getSampleRate()!=0.0) {
				sampleRate = rdp.getSampleRate();
			}
			if ((rdp.getSampleRate()!=0.0) && (rdp.getSampleRate()!= sampleRate)) {
				throw new TraceViewException("PlotDataProviderCalculated: different sampleRates in channels list");
			}
		}
		this.sources = sources;
	}

	public double getSampleRate() {
		return sources.get(0).getSampleRate();
	}

	/**
	 * @param endTime
	 *            the endTime to set Used for network channels before data
	 *            retrieving
	 */
	public void setEndTime(long endTime) {
		for (IChannel rdp : sources) {
			rdp.setEndTime(endTime);
		}
	}

	private List<TimeInterval> intersect2Channels(List<TimeInterval> rdp1intervals, IChannel rdp2) {
		List<TimeInterval> ret = new ArrayList<TimeInterval>();
		for (TimeInterval ti : rdp1intervals) {
			for (Segment segm2 : rdp2.getRawData()) {
				if ((segm2 != null) && ti.isIntersect(segm2.getTimeRange())) {
					ret.add(TimeInterval.getIntersect(ti, segm2.getTimeRange()));
				}
			}
		}
		return ret;
	}

	private List<TimeInterval> intersectChannels() {
		List<TimeInterval> tis = new ArrayList<TimeInterval>();
		for (Segment segment : sources.get(0).getRawData()) {
			tis.add(segment.getTimeRange());
		}
		for (int i = 1; i < sources.size(); i++) {
			tis = intersect2Channels(tis, sources.get(i));
		}
		return tis;
	}

	/**
	 * Getter of the property <tt>rawData</tt>
	 * 
	 * @return Returns all raw data this provider contains.
	 * @uml.property name="rawData"
	 */
	public List<Segment> getRawData() {
		lg.debug("PlotDataProviderCalculated.getRawData: " + toString());
		if (sources.size() == 1) {
			return sources.get(0).getRawData();
		} else {
			List<Segment> ret = new ArrayList<Segment>();
			try {
				int i = 0;
				for (TimeInterval segmentTi : intersectChannels()) {
					ret.add(calculateData(segmentTi, i++));
				}
			} catch (Exception e) {
				lg.error("Can't get calculated segment: " + e.getMessage());
			}
			return ret;
		}
	}

	/**
	 * @return Returns the raw data this provider contains for the time window.
	 * @uml.property name="rawData"
	 */
	public List<Segment> getRawData(TimeInterval ti) {
		lg.debug("PlotDataProviderCalculated.getRawData: " + toString() + "; " + ti);
		if (sources.size() == 1) {
			return sources.get(0).getRawData();
		} else {
			List<Segment> ret = new ArrayList<Segment>();
			try {
				int i = 0;
				for (TimeInterval segmentTi : intersectChannels()) {
					if (ti.isIntersect(segmentTi)) {
						ret.add(calculateData(TimeInterval.getIntersect(ti, segmentTi), i++));
					}
				}
			} catch (TraceViewException e) {
				lg.error("Can't get calculated segment: " + e.toString());
			}
			return ret;
		}
	}
	
	public void deleteRawData(TimeInterval ti) {
		setLastUsedPixelizeArguments(null);
		pointsCache = null;
	}

	public int getDataLength(TimeInterval ti) {
		List<TimeInterval> tis = intersectChannels();
		int dataLength = 0;
		for (TimeInterval tiSegment : tis) {
			dataLength += tiSegment.getDuration() * getSampleRate();
		}
		return dataLength;
	}

	/**
	 * @return count of {@link Segment}s this provider contains
	 */
	public int getSegmentCount() {
		return intersectChannels().size();
	}

	/**
	 * Add segment to raw data provider
	 * 
	 * @param segment
	 *            to add
	 */
	public void addSegment(Segment segment) {
		lg.debug("PlotDataProviderCalculated.addSegment stub: " + toString() + "; " + segment);
	}

	/**
	 * @return time range of contained data
	 */
	public TimeInterval getTimeRange() {
		List<TimeInterval> tis = intersectChannels();
		if (tis.size() == 0) {
			return null;
		} else {
			return new TimeInterval(tis.get(0).getStart(), tis.get(tis.size()).getEnd());
		}
	}

	/**
	 * clears this provider, drops all data
	 */
	public void drop() {
		lg.debug("PlotDataProviderCalculated.drop stub: " + toString());
	}

	/**
	 * @return flag if data loading process was started for this provider
	 */
	public boolean isLoadingStarted() {
		for (IChannel rdp : sources) {
			if (rdp.isLoadingStarted())
				return true;
		}
		return false;
	}

	public void setLoadingStarted(boolean loading) {
		lg.debug("PlotDataProviderCalculated.setLoadingStarted stub: " + toString());
	}

	/**
	 * @return flag is data provider loaded
	 */
	public boolean isLoaded() {
		for (IChannel rdp : sources) {
			if (rdp.isLoaded())
				return true;
		}
		return false;
	}

	public void setLoaded(boolean loaded) {
		lg.debug("PlotDataProviderCalculated.setLoaded stub: " + toString());
	}

	/**
	 * Load data into this data provider from data sources
	 * 
	 * @param ti
	 */
	public void loadData(TimeInterval ti) {
		for (IChannel rdp : sources) {
			rdp.loadData(ti);
		}
	}

	/**
	 * @return list of data sources
	 */
	public List<ISource> getSources() {
		List<ISource> ret = new ArrayList<ISource>();
		for (IChannel rdp : sources) {
			List<ISource> providerSources = rdp.getSources();
			for (ISource src : providerSources) {
				if (!ret.contains(src)) {
					ret.add(src);
				}
			}
		}
		return ret;
	}

	/**
	 * @param date
	 * @return data source contains data on this time
	 */
	public ISource getSource(Date date) {
		lg.debug("PlotDataProviderCalculated.getSource stub: " + toString() + " " + date);
		return null;
	}

	/**
	 * Sets data stream to serialize this provider
	 * 
	 * @param dataStream
	 */
	public void setDataStream(Object dataStream) {
		lg.debug("PlotDataProviderCalculated.setDataStream stub: " + toString());
	}

	/**
	 * @return data stream where this provider was serialized
	 */
	public BufferedRandomAccessFile getDataStream() {
		lg.debug("PlotDataProviderCalculated.getDataStream stub: " + toString());
		return null;
	}

	/**
	 * @return flag if this provider was serialized
	 */
	public boolean isSerialized() {
		return false;
	}

	/**
	 * Loads all data to this provider from it's data sources
	 */
	public void load() {
		lg.debug("PlotDataProviderCalculated.load " + this);
		for (IChannel rdp : sources) {
			rdp.load();
		}
	}

	/**
	 * Loads data inside given time interval to this provider from it's data
	 * sources
	 */
	public void load(TimeInterval ti) {
		lg.debug("PlotDataProviderCalculated.load " + this + "; " + ti);
		for (IChannel rdp : sources) {
			rdp.load(ti);
		}
	}

	/**
	 * @return string representation of data provider in debug purposes
	 */
	public String toString() {
		String ret = "";
		for (IChannel rdp : sources) {
			ret = ret + rdp.toString() + ";";
		}
		return "PlotDataProviderCalculated: " + ret;
	}

	/**
	 * Sorts data provider after loading
	 */
	public void sort() {
		// setting channel serial numbers in segments
		Segment previousSegment = null;
		int segmentNumber = 0;
		int sourceNumber = 0;
		int continueAreaNumber = 0;
		for (Segment segment : getRawData()) {
			if (previousSegment != null) {
				if (Segment.isDataBreak(previousSegment.getEndTime().getTime(), segment.getStartTime().getTime(), segment.getSampleRate())) {
					segmentNumber++;
				}
				if (Segment.isDataGap(previousSegment.getEndTime().getTime(), segment.getStartTime().getTime(), segment.getSampleRate())) {
					continueAreaNumber++;
				}
				if (!previousSegment.getDataSource().equals(segment.getDataSource())) {
					sourceNumber++;
				}
			}
			previousSegment = segment;
			segment.setChannelSerialNumber(segmentNumber);
			segment.setSourceSerialNumber(sourceNumber);
			segment.setContinueAreaNumber(continueAreaNumber);
		}
	}

	/**
	 * Prints RawDataProvider content
	 */
	public void printout() {
		System.out.println("  " + toString());
		for (Segment segment : getRawData()) {
			System.out.println("    " + segment.toString());
		}
	}

	public boolean isNetworkDataProvider() {
		for (IChannel rdp : sources) {
			if (rdp.isNetworkDataProvider())
				return true;
		}
		return false;
	}

	@Override
	public void update(Observable arg0, Object loaded) {
		for (IChannel rdp : sources) {
			rdp.update(arg0, loaded);
		}
	}

	/**
	 * Special serialization handler
	 * 
	 * @param out
	 *            stream to serialize this object
	 * @see Serializable
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		lg.debug("PlotDataProviderCalculated.writeObject stub: " + toString());
	}

	/**
	 * Special deserialization handler
	 * 
	 * @param in
	 *            stream to deserialize object
	 * @see Serializable
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		lg.debug("PlotDataProviderCalculated.readObject stub: " + toString());
	}
}
