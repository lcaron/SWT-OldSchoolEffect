/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Laurent CARON (laurent.caron at gmail dot com) - Initial Contributor
 *******************************************************************************/
package org.mihalis.demos.scrolls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class StarwarsScroll {

	// Title
	private static final String SHELL_TITLE = "StarwarsScroll";

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 50;

	// Width, Height
	private static final int CANVAS_WIDTH = 480;
	private static final int CANVAS_HEIGHT = 360;

	private Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private Image image;

	//
	private String[] text = { "ligne 1", "ligne 1 fgsdfsdfgsdf", "ligne 1 sdfg sdg sdfg  sgf s seg", "ligne 1", "ligne 1 sdfgsdfgsdfgdg", "ligne 1", "ligne 1", "ligne 1", "ligne 1", "ligne 1", "ligne 1", "ligne 1", "ligne 1", "ligne 1", "ligne 1",
			"ligne 1", "ligne 1", "ligne 1", "ligne 1" };
	private static final String FONT = "Lucida Sans";
	private static final int FONT_SIZE = 16;
	private int y;
	private static final int STEP = 5;
	private PerspectiveFilter filter;

	public StarwarsScroll(Display display) {
		this.display = display;
	}

	public void init() {

		image = new Image(display, w, h);
		y = h + 10;

		filter = new PerspectiveFilter();
		filter.setCorners(w / 2 - 50, 0, w / 2 + 50, 0, w - 1, h - 1, 0, h - 1);

		redrawCanvas();
	}

	public void animate() {

		// Clear screen
		final Image imageTmp = new Image(display, w, h);
		final GC gcT = new GC(imageTmp);
		gcT.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		gcT.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		gcT.fillRectangle(0, 0, w, h);
		final Font font = new Font(display, FONT, FONT_SIZE, SWT.NORMAL);
		gcT.setFont(font);
		gcT.setAdvanced(true);
		gcT.setAntialias(SWT.ON);

		// Draw text
		int currentY = y;
		for (final String current : text) {
			final Point textSize = gcT.stringExtent(current);
			final int x = (w - textSize.x) / 2;
			gcT.drawString(current, x, currentY);
			currentY += textSize.y * 1.5;
		}

		if (currentY <= 0) {
			y = h + 10;
		} else {
			y -= STEP;
		}

		// Now let's play with imageTmp
		image = new Image(display, filter.filter(imageTmp.getImageData(), null));

		font.dispose();
		gcT.dispose();
		imageTmp.dispose();
		redrawCanvas();
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(SHELL_TITLE);
		shell.setLayout(new GridLayout(1, false));

		canvas = new Canvas(shell, SWT.BORDER | SWT.NO_REDRAW_RESIZE);
		final GridData gdCanvas = new GridData(GridData.FILL, GridData.FILL, true, true);
		gdCanvas.widthHint = CANVAS_WIDTH;
		gdCanvas.heightHint = CANVAS_HEIGHT;
		canvas.setLayoutData(gdCanvas);

		gc = new GC(canvas);

		canvas.addListener(SWT.Resize, e -> {
			w = canvas.getClientArea().width;
			h = canvas.getClientArea().height;
			init();
		});

		canvas.addPaintListener(e -> {
			redrawCanvas();
		});
		return shell;
	}

	private void redrawCanvas() {
		if (image != null) {
			gc.drawImage(image, 0, 0);
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();

		final StarwarsScroll app = new StarwarsScroll(display);

		final Shell shell = app.createWindow();
		shell.pack();
		shell.open();
		app.init();

		// Set up the timer for the animation
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				app.animate();
				display.timerExec(TIMER_INTERVAL, this);
			}
		};

		// Launch the timer
		display.timerExec(TIMER_INTERVAL, runnable);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		// Kill the timer
		display.timerExec(-1, runnable);
		display.dispose();
	}
}
