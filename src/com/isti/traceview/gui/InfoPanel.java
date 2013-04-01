package com.isti.traceview.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jfree.ui.RectangleEdge;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import net.miginfocom.swing.MigLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Left panel for auxiliary information: selection checkbox, axis painting
 * etc
 */
class InfoPanel extends JPanel implements ItemListener {
	private NonPrintableCheckBox selected = null;
	private Slot channelView = null;
	JLabel lblNt = null;

	public InfoPanel(Slot cv, int infoPanelWidth, boolean isDrawSelectionCheckBox, Color infoAreaBgColor) {
		super();
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				positionLabel();
			}
		});
		if (infoAreaBgColor != null) {
			setBackground(infoAreaBgColor);
		}
		this.channelView = cv;
		setPreferredSize(new Dimension(299, 413));
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(null);
		//if (isDrawSelectionCheckBox) {
			add(getSelected());

			
			JCheckBox checkBox = new JCheckBox("");
			checkBox.setBounds(9, 9, 27, 26);
			add(checkBox);
			
			lblNt = new JLabel("nT");
			positionLabel();
			add(lblNt);
		//}
	}
	
	private void positionLabel(){
		lblNt.setBounds(9, getHeight()-30, 19, 14);
		System.out.println("getHeight()");
		repaint();
	}

	public boolean isSelected() {
		return selected.isSelected();
	}

	/**
	 * This method initializes selected
	 * 
	 * @return Customized checkbox for Slot selection which drawn on
	 *         screen and don't drawn during rendering for print
	 */
	private NonPrintableCheckBox getSelected() {

		return new NonPrintableCheckBox();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
//		axis.draw((Graphics2D) g, getWidth() - 5, new Rectangle(0, 0, getWidth(), getHeight()), new Rectangle(0, 0, getWidth(), getHeight()),
//				RectangleEdge.LEFT, null);
	}

	private class NonPrintableCheckBox extends JCheckBox {
		public void paint(Graphics g) {
			// if(!isPaintingForPrint()){ //works only in jre 1.6
			super.paint(g);
			// }
		}
	}

	/** Listens to the check box. */
	public void itemStateChanged(ItemEvent e) {
//		if (!(graphPanel.getSelectState() || graphPanel.getOverlayState())) {
//			if (e.getStateChange() == ItemEvent.DESELECTED) {
//				selectionNumber = 0;
//				graphPanel.getSelectedChannelShowSet().remove(channelView);
//			} else if (e.getStateChange() == ItemEvent.SELECTED) {
//				graphPanel.getSelectedChannelShowSet().add(channelView);
//				currentSelectionNumber++;
//				selectionNumber = currentSelectionNumber;
//			}
//		}
//		selectionPerformed();
	}
}