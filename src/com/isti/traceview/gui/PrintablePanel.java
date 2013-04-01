package com.isti.traceview.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JPanel;
import javax.swing.RepaintManager;

public class PrintablePanel extends JPanel implements Printable {
	
	@Override
    public void print(Graphics g) {
        Color orig = getBackground();
        setBackground(Color.WHITE);

        // wrap in try/finally so that we always restore the state
        try {
            super.print(g);
        } finally {
            setBackground(orig);
        }
    }

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex > 0) {
			return Printable.NO_SUCH_PAGE;
		}
		Graphics2D g2 = (Graphics2D) graphics;
		double x = pageFormat.getImageableX();
		double y = pageFormat.getImageableY();
		g2.translate(x, y);
		RepaintManager currentManager = RepaintManager.currentManager(this);
		currentManager.setDoubleBufferingEnabled(false);
		g2.scale(g2.getClipBounds().width / new Double(this.getWidth()), g2.getClipBounds().height / new Double(this.getHeight()));
		this.print(g2);
		currentManager.setDoubleBufferingEnabled(true);
		return Printable.PAGE_EXISTS;
	}
}
