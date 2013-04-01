package com.isti.traceview.gui.controls;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.isti.traceview.common.TimeInterval;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyListener;

public class DurationSelector extends JPanel implements Observer, ItemListener {
	private static Logger lg = Logger.getLogger(DurationSelector.class);
	protected static final long LENGTH_SECOND = 1000;
	protected static final long LENGTH_MINUTE = 60000;
	protected static final long LENGTH_HOUR = 3600000;
	protected static final long LENGTH_DAY = 86400000;
	
	DateSelector dateSelector = null;
	private JFormattedTextField formattedTextField;
	private JComboBox unitsCB;
	private Long oldDuration = null;

	public enum Units {
		Second, Minute, Hour, Day, Month
	}

	public DurationSelector(DateSelector dateSelector) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.dateSelector = dateSelector;
		JLabel labelL = new JLabel("Duration: ");
		labelL.setPreferredSize(new Dimension(55, 14));
		labelL.setMinimumSize(new Dimension(55, 14));
		labelL.setMaximumSize(new Dimension(55, 14));
		labelL.setSize(new Dimension(55, 14));
		add(labelL);

		formattedTextField = new JFormattedTextField(new DecimalFormat("###0")) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.JFormattedTextField#commitEdit()
			 */
			@Override
			public void commitEdit() throws ParseException {
				super.commitEdit();
				if (isEditValid()) {
					fireDurationChange();
				}
			}
		};
		formattedTextField.setPreferredSize(new Dimension(50, 20));
		formattedTextField.setMinimumSize(new Dimension(30, 20));
		formattedTextField.setMaximumSize(new Dimension(70, 30));
		formattedTextField.setInputVerifier(new NumberInputVerifier());
		formattedTextField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		add(formattedTextField);

		unitsCB = new JComboBox();
		unitsCB.setBorder(null);
		unitsCB.addItemListener(this);
		unitsCB.setPreferredSize(new Dimension(70, 20));
		unitsCB.setMinimumSize(new Dimension(50, 20));
		unitsCB.setMaximumSize(new Dimension(32767, 30));
		unitsCB.setModel(new DefaultComboBoxModel(Units.values()));
		add(unitsCB);
	}

	public Long getDuration() {
		Long value = (Long) formattedTextField.getValue();
		switch (getUnitsSelected()) {
		case Second:
			return value == null ? null : (value * LENGTH_SECOND);
		case Minute:
			return value == null ? null : (value * LENGTH_MINUTE);
		case Hour:
			return value == null ? null : (value * LENGTH_HOUR);
		case Day:
			return value == null ? null : (value * LENGTH_DAY);
		case Month:
			TimeInterval range = getMonthRange(dateSelector.getDate(), getCountSelected());
			return range.getDuration();
		default:
			return null;
		}

	}

	public void setDuration(Long duration) {
		setDuration(duration, true);
	}

	private void setDuration(Long duration, boolean fireUpdate) {
		if (getUnitsSelected() != Units.Month) {
			Long od = getDuration();
			oldDuration = duration;
			if (duration % (LENGTH_DAY) == 0) {
				formattedTextField.setValue(duration / (LENGTH_DAY));
				unitsCB.setSelectedItem(Units.Day);
			} else if (duration % (LENGTH_HOUR) == 0) {
				formattedTextField.setValue(duration / (LENGTH_HOUR));
				unitsCB.setSelectedItem(Units.Hour);
			} else if (duration % (LENGTH_MINUTE) == 0) {
				formattedTextField.setValue(duration / (LENGTH_MINUTE));
				unitsCB.setSelectedItem(Units.Minute);
			} else {
				formattedTextField.setValue(duration / LENGTH_SECOND);
				unitsCB.setSelectedItem(Units.Second);
			}
			if (fireUpdate && !(od==null || getDuration().equals(od))) {
				fireDurationChange();
			}
		}
	}

	public void addDurationListener(DurationListener listener) {
		listenerList.add(DurationListener.class, listener);
	}

	public void removeDurationListener(DurationListener listener) {
		listenerList.remove(DurationListener.class, listener);
	}

	public Units getUnitsSelected() {
		return (Units) unitsCB.getSelectedItem();
	}

	public int getCountSelected() {
		return ((Long) formattedTextField.getValue()).intValue();
	}

	protected void fireDurationChange() {
		oldDuration = getDuration();
		Units selected = getUnitsSelected();
		if (selected.equals(Units.Month)) {
			TimeInterval range = getMonthRange(dateSelector.getDate(), getCountSelected());
			dateSelector.setDate(range.getStartTime());
			dateSelector.repaint();
		}
		// Guaranteed to return a non-null array

		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == DurationListener.class) {
				DurationEvent durationEvent;
				durationEvent = new DurationEvent(this, getDuration());
				((DurationListener) listeners[i + 1]).durationChanged(durationEvent);
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof TimeInterval) {
			TimeInterval ti = (TimeInterval) arg;
			if ((getDuration() == null) || !(ti.getDuration() == getDuration().longValue())) {
				lg.debug("DurationSelector.update");
				setDuration(new Long(ti.getDuration()), false);
				repaint();
			}
		}
	}

	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.SELECTED) {
			Units selected = getUnitsSelected();
			if (selected == Units.Month) {
				formattedTextField.setValue(1L);
			} else {
				long newValue=1;
				if(oldDuration!=null){
					if(selected==Units.Day){
						newValue=oldDuration / (LENGTH_DAY);
					} else if(selected==Units.Hour){
						newValue=oldDuration / (LENGTH_HOUR);
					} else if(selected==Units.Minute){
						newValue=oldDuration / (LENGTH_MINUTE);
					} else if(selected==Units.Second){
						newValue=oldDuration / (LENGTH_SECOND);
					}	
				}
				if(newValue==0) newValue=1;
				formattedTextField.setValue(newValue);
			}
			fireDurationChange();
		} 
	}

	protected JFormattedTextField getFormattedTextField() {
		return formattedTextField;
	}

	protected JComboBox getUnitsCB() {
		return unitsCB;
	}

	public TimeInterval getNextMonthRange() {
		TimeInterval currentRange = getMonthRange(dateSelector.getDate(), getCountSelected());
		return getMonthRange(new Date(currentRange.getEnd() + 1000), getCountSelected());
	}

	public TimeInterval getPreviousMonthRange() {
		TimeInterval currentRange = getMonthRange(dateSelector.getDate(), getCountSelected());
		Calendar today = new GregorianCalendar();
		today.setTime(currentRange.getStartTime());
		Calendar dayStart = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH) - getCountSelected(), 1, 0, 0, 0);
		Calendar dayEnd = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), 1, 0, 0, 0);
		return new TimeInterval(dayStart.getTime(), dayEnd.getTime());
	}
	
	public void addKeyListener(KeyListener l){
		formattedTextField.addKeyListener(l);
		unitsCB.addKeyListener(l);
	}

	public static TimeInterval getMonthRange(Date date, int monthsCount) {
		Calendar today = new GregorianCalendar();
		today.setTime(date);
		Calendar dayStart = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), 1, 0, 0, 0);
		Calendar dayEnd = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + monthsCount, 1, 0, 0, 0);
		return new TimeInterval(dayStart.getTime(), dayEnd.getTime());
	}
	
	public static TimeInterval getDayRange(Date date, int daysCount) {
		Calendar today = new GregorianCalendar();
		today.setTime(date);
		Calendar dayStart = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		Calendar dayEnd = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)+daysCount, 0, 0, 0);
		return new TimeInterval(dayStart.getTime(), dayEnd.getTime());
	}

	private class NumberInputVerifier extends InputVerifier {
		public boolean verify(JComponent input) {
			if (input instanceof JFormattedTextField) {
				JFormattedTextField ftf = (JFormattedTextField) input;
				AbstractFormatter formatter = ftf.getFormatter();
				if (formatter != null) {
					String text = ftf.getText();
					try {
						formatter.stringToValue(text);
						return true;
					} catch (ParseException pe) {
						return false;
					}
				}
			}
			return true;
		}

		public boolean shouldYieldFocus(JComponent input) {
			return verify(input);
		}
	}
}