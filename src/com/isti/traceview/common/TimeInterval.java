package com.isti.traceview.common;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.isti.traceview.TraceView;

/**
 * Class to represent interval of time
 * 
 * @author Max Kokoulin
 */

public class TimeInterval {
	/**
	 * Enumeration for string time representation formats. We use formats with different accuracy
	 * for convinient date plotting in different situations
	 */
	public enum DateFormatType {
		/**
		 * format mm:ss.SSS
		 */
		DATE_FORMAT_SHORT,
		/**
		 * format HH:mm:ss
		 */
		DATE_FORMAT_NORMAL,

		/**
		 * format DDD,HH:mm:ss
		 */
		DATE_FORMAT_MIDDLE,

		/**
		 * format yyyy,DDD,HH:mm
		 */
		DATE_FORMAT_LONG,
		/**
		 * format yyyy,DDD,HH:mm:ss.SSS
		 */
		DATE_FORMAT_FULL;
	};

	private static Logger lg = Logger.getLogger(TimeInterval.class);
	public static SimpleDateFormat df_1 = new SimpleDateFormat("mm:ss.SSS");
	public static SimpleDateFormat df_2 = new SimpleDateFormat("HH:mm:ss");
	public static SimpleDateFormat df_3 = new SimpleDateFormat("DDD,HH:mm");
	public static SimpleDateFormat df_3_g = new SimpleDateFormat("MM/dd,HH:mm");
	public static SimpleDateFormat df_4 = new SimpleDateFormat("DDD");
	public static SimpleDateFormat df_4_g = new SimpleDateFormat("MM/dd");
	public static SimpleDateFormat df_5 = new SimpleDateFormat("yyyy,DDD");
	public static SimpleDateFormat df_5_g = new SimpleDateFormat("MM/dd/yyyy");
	public static SimpleDateFormat df_full = new SimpleDateFormat("yyyy,DDD,HH:mm:ss.SSS");
	public static SimpleDateFormat df_g_full = new SimpleDateFormat("MM/dd/yyyy,HH:mm:ss.SSS");
	static {
		df_1.setTimeZone(TraceView.timeZone);
		df_2.setTimeZone(TraceView.timeZone);
		df_3.setTimeZone(TraceView.timeZone);
		df_5.setTimeZone(TraceView.timeZone);
		df_full.setTimeZone(TraceView.timeZone);
		df_3_g.setTimeZone(TraceView.timeZone);
		df_5_g.setTimeZone(TraceView.timeZone);
		df_g_full.setTimeZone(TraceView.timeZone);
	}
	private long startTime;
	private long endTime;

	/**
	 * Default constructor
	 */
	public TimeInterval() {
		startTime = Long.MAX_VALUE;
		endTime = Long.MIN_VALUE;
	}

	/**
	 * Constructor from Java standard time values
	 * 
	 * @param startTime
	 *            start time of interval in milliseconds (Java standard time)
	 * @param endTime
	 *            end time of interval in milliseconds (Java standard time)
	 */
	public TimeInterval(long startTime, long endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * Constructor from Java {@link Date} values
	 * 
	 * @param startTime
	 *            startTime start time of interval
	 * @param endTime
	 *            end time of interval
	 */
	public TimeInterval(Date startTime, Date endTime) {
		this(startTime.getTime(), endTime.getTime());
	}

	/**
	 * Extends time interval
	 * 
	 * @param date
	 *            new end value
	 */
	public void setMaxValue(Date date) {
		long newVal = date.getTime();
		if (newVal > endTime) {
			endTime = newVal;
		}
	}

	/**
	 * Extends time interval
	 * 
	 * @param date
	 *            new start value
	 */
	public void setMinValue(Date date) {
		long newVal = date.getTime();
		if (newVal < startTime) {
			startTime = newVal;
		}
	}

	/**
	 * Getter of startTime
	 * 
	 * @return start time of interval
	 */
	public Date getStartTime() {
		return new Date(startTime);
	}

	/**
	 * Getter of startTime
	 * 
	 * @return start time of interval in Java standard time form
	 */
	public long getStart() {
		return startTime;
	}

	/**
	 * Getter of endTime
	 * 
	 * @return end time of interval
	 */
	public Date getEndTime() {
		return new Date(endTime);
	}

	/**
	 * Getter of endTime
	 * 
	 * @return end time of interval in Java standard time form
	 */
	public long getEnd() {
		return endTime;
	}

	/**
	 * @return duration of interval in milliseconds
	 */
	public long getDuration() {
		return endTime - startTime;
	}

	/**
	 * @param date
	 *            time to test
	 * @return flag if this interval contains given time
	 */
	public boolean isContain(Date date) {
		return isContain(date.getTime());
	}

	/**
	 * @param date
	 *            time to test in Java standard time form
	 * @return flag if this interval contains given time
	 */
	public boolean isContain(long date) {
		return (startTime <= date && date <= endTime);
	}

	/**
	 * @param ti
	 *            time interval to test
	 * @return flag if this interval contains given one
	 */
	public boolean isContain(TimeInterval ti) {
		return (startTime <= ti.getStart() && endTime >= ti.getEnd());
	}

	/**
	 * @param range
	 *            time interval to test
	 * @return flag if this interval intersects with given one
	 */
	public boolean isIntersect(TimeInterval range) {
		if (range.getStartTime() == null || range.getEndTime() == null || getStartTime() == null || getEndTime() == null) {
			return true;
		} else {
			return !((startTime >= range.getEndTime().getTime() && endTime >= range.getEndTime().getTime()) || (startTime <= range.getStartTime()
					.getTime() && endTime <= range.getStartTime().getTime()));
		}
	}

	/**
	 * String representation of time interval in the debugging purposes
	 */
	public String toString() {
		return "start time: " + formatDate(getStartTime(), DateFormatType.DATE_FORMAT_FULL) + ", end time: "
				+ formatDate(getEndTime(), DateFormatType.DATE_FORMAT_FULL);
	}

	/**
	 * String representation of this time interval's duration
	 * 
	 * @see TimeInterval#getStringDiff
	 */
	public String convert() {
		return TimeInterval.getStringDiff(getDuration());
	}

	/**
	 * String representation of this time interval's duration
	 * 
	 * @see TimeInterval#getStringDiffDDHHMMSS
	 */
	public String convertDDHHMMSS() {
		return TimeInterval.getStringDiffDDHHMMSS(getDuration());
	}

	/**
	 * String representation of duration in seconds (if duration less then hour), hours (if duration less then day) or decimal days
	 */
	public static String getStringDiff(long duration) {
		//lg.debug("TimeInterval.getStringDiff: duration="+ duration);
		String ret = "";
		if (duration < 0) {
			duration = -duration;
			ret = "-";
		} else {
			ret = "+";
		}
		if(duration < 86400000) {
			if (duration < 3600000) {
				Double sec = new Double(duration) / 1000;
				ret = ret + sec.toString() + " s";
			} else {
				Double h = new Double(duration) / 3600000;

				ret = ret + new DecimalFormat("#######.###").format(h) + " h";
			}
		} else {
			Double days = new Double(duration) / 86400000;
			ret = ret + new DecimalFormat("#######.###").format(days) + " d";
		}
		//lg.debug("TimeInterval.getStringDiff return: "+ ret);
		return ret;
	}

	/**
	 * String representation of duration in the form +-##days ##hours ##min ##.## s
	 */
	public static String getStringDiffDDHHMMSS(long duration) {
		//lg.debug("TimeInterval.getStringDiffDDHHMMSS: duration="+ duration);
		String ret = "";
		if (duration < 0) {
			duration = -duration;
			ret = "-";
		}
		long days = duration / 86400000;
		if (days > 0) {
			ret = ret + days + " days";
		}
		long rest = duration % 86400000;

		long hour = rest / 3600000;
		if (hour > 0) {
			if (ret.length() > 1) {
				ret = ret + ", ";
			}
			ret = ret + hour + " hours";
		}
		rest = rest % 3600000;
		long min = rest / 60000;
		if (min > 0) {
			if (ret.length() > 1) {
				ret = ret + ", ";
			}
			ret = ret + min + " min";
		}
		rest = rest % 60000;
		double sec = new Double(rest) / 1000;
		if (ret.length() > 1) {
			ret = ret + ", ";
		}
		ret = ret + sec + " s";
		//lg.debug("TimeInterval.getStringDiffDDHHMMSS return: "+ ret);
		return ret;
	}

	/**
	 * Intersect two time intervals
	 * 
	 * @return time interval which is intersection of two given time intervals, or null
	 */
	public static TimeInterval getIntersect(TimeInterval ti1, TimeInterval ti2) {
		if (ti1 == null || ti2 == null)
			return null;
		long start = Math.max(ti1.getStart(), ti2.getStart());
		long end = Math.min(ti1.getEnd(), ti2.getEnd());
		if (end > start) {
			return new TimeInterval(new Date(start), new Date(end));
		} else {
			return null;
		}
	}

	/**
	 * Aggregate two time intervals
	 * 
	 * @return time interval which aggregate two given time intervals, or null
	 */
	public static TimeInterval getAggregate(TimeInterval ti1, TimeInterval ti2) {
		if (ti1 == null || ti2 == null)
			return null;
		long start = Math.min(ti1.getStart(), ti2.getStart());
		long end = Math.max(ti1.getEnd(), ti2.getEnd());
		if (end > start) {
			return new TimeInterval(new Date(start), new Date(end));
		} else {
			return null;
		}
	}

	/**
	 * Constructs time from integer values
	 * 
	 * @param year
	 * @param jday
	 * @param hour_of_day
	 * @param minute
	 * @param second
	 * @param millisecond
	 * @return time in Java standard form
	 */
	public static long getTime(int year, int jday, int hour_of_day, int minute, int second, int millisecond) {
		GregorianCalendar cal = new GregorianCalendar(TraceView.timeZone);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.DAY_OF_YEAR, jday);
		cal.set(Calendar.HOUR_OF_DAY, hour_of_day);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, millisecond);
		return cal.getTimeInMillis();
	}
	
	public TimeInterval getIncludingDayInterval(){
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(getStart());
		GregorianCalendar calRet = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0);
		long start = calRet.getTimeInMillis();
		cal.setTimeInMillis(getEnd()-1);
		calRet = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0);
		long end = calRet.getTimeInMillis()+86400000;
		return new TimeInterval(start, end);
		
	}

	/**
	 * Parse string to get date according given date format
	 * 
	 * @param date
	 *            string representation of date
	 * @param type
	 *            date format
	 * @return parsed date
	 */
	public static Date parseDate(String date, DateFormatType type) {
		Date ret = null;
		try {
			switch (type) {
			case DATE_FORMAT_SHORT:
				ret = df_1.parse(date);
				break;
			case DATE_FORMAT_NORMAL:
				ret = df_2.parse(date);
				break;
			case DATE_FORMAT_MIDDLE:
				ret = df_3.parse(date);
				break;
			case DATE_FORMAT_LONG:
				ret = df_5.parse(date);
				break;
			case DATE_FORMAT_FULL:
				ret = df_full.parse(date);
				break;
			default:
				lg.error("Wrong date format type: " + type);
			}
		} catch (ParseException e) {
			lg.error("Cant parse date from string " + date + "; " + e);
		}
		return ret;
	}

	/**
	 * Gets string representation of date according given date format
	 * 
	 * @param date
	 *            date to process
	 * @param type
	 *            date format
	 * @return string representation of date
	 */
	public static String formatDate(Date date, DateFormatType type) {
		switch (type) {
		case DATE_FORMAT_SHORT:
			return df_1.format(date);
		case DATE_FORMAT_NORMAL:
			return df_2.format(date);
		case DATE_FORMAT_MIDDLE:
			return df_3.format(date);
		case DATE_FORMAT_LONG:
			return df_5.format(date);
		case DATE_FORMAT_FULL:
			return df_full.format(date);
		default:
			lg.error("Wrong date format type: " + type);
			return null;
		}
	}
	
	 public boolean equals(Object obj){
		 if(!(obj instanceof TimeInterval)) return false;
		 else {
			 TimeInterval ti = (TimeInterval)obj;
			 return (startTime==ti.startTime) && (endTime==ti.endTime);
		 }
	 }
}