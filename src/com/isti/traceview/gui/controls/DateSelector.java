package com.isti.traceview.gui.controls;

import org.apache.log4j.Logger;
import org.freixas.jcalendar.JCalendar;
import org.freixas.jcalendar.JCalendarCombo;

import com.isti.traceview.common.TimeInterval;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class DateSelector extends JCalendarCombo implements Observer {
	private static Logger lg = Logger.getLogger(DateSelector.class); 
	private static SimpleDateFormat	df = new SimpleDateFormat("yyyy/MM/dd(DDD) HH:mm", Locale.getDefault());
	boolean wasUpdated = false;
	
	public DateSelector(Calendar calendar){
		super(calendar, Locale.getDefault(), JCalendar.DISPLAY_DATE | JCalendar.DISPLAY_TIME, false, "HH:mm");
		setDateFormat(df);
		setNullAllowed(false);
		setEditable(false);
	}

	@Override
	public void update(Observable o, Object arg) {
		if(arg instanceof TimeInterval){
			TimeInterval ti = (TimeInterval)arg;
			if(!ti.getStartTime().equals(getDate())){
				lg.debug("DateSelector.update");
				setDate(ti.getStartTime());
				repaint();
			}
		}
	}
	
	public static TimeInterval getDayRange(Date date) {
		Calendar today = new GregorianCalendar();
		today.setTime(date);
		Calendar dayStart = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		return new TimeInterval(dayStart.getTime(), new Date(dayStart.getTime().getTime() + 3600 * 24 * 1000));
	}
}
