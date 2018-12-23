/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ? - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.wave;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class WaveEffect {

	private static final String SHELL_TITLE = "WaveEffect";

	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 480;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private Image image;

	//
	private int patternWidth;
	private int patternHeight;
	private int centerX;
	private int centerY;
	private int[][] xPos;
	private int[][] yPos;
	private double position;
	private static final double STEP = 0.07000000000000001D;
	private static final int COLS = 30;
	private static final int LINES = 20;
	private int kind = 0;
	private boolean listenerAlreadyAdded;

	public WaveEffect(Display display) {
		this.display = display;
	}

	public void init() {
		final ImageData imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
		image = new Image(display, imageData);

		position = 0.0D;
		xPos = new int[COLS][LINES];
		yPos = new int[COLS][LINES];

		patternWidth = (int) (0.5D * (w - (COLS - 1) * 15)) - 3;
		patternHeight = (int) (0.5D * (h - (LINES - 1) * 15)) - 3;
		centerX = (int) (0.5D * w);
		centerY = (int) (0.5D * h);

		if (!listenerAlreadyAdded) {
			listenerAlreadyAdded = true;
			canvas.addListener(SWT.MouseUp, new Listener() {

				@Override
				public void handleEvent(Event event) {
					position = 0.0D;
					kind++;
					if (kind == 4) {
						kind = 0;
					}
				}
			});
		}

		redrawCanvas();
	}

	public void animate() {

		// Clear screen
		final GC GCTemp = new GC(image);
		GCTemp.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		GCTemp.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		GCTemp.fillRectangle(0, 0, w, h);

		// Compute new positions
		position += STEP;
		for (int i = 0; i < COLS; ++i) {
			for (int j = 0; j < LINES; ++j) {
				if (kind == 0) {
					xPos[i][j] = (int) (patternWidth + 15 * i + 10.0D * Math.cos(position * (1.0D + 0.01D * i + 0.015D * j)));
					yPos[i][j] = (int) (patternHeight + 15 * j - 10.0D * Math.sin(position * (1.0D + 0.0123D * j + 0.012D * i)));
				}
				if (kind == 1) {
					xPos[i][j] = (int) (patternWidth + 15 * i + 20.0D * Math.sin(position * (1.0D + 0.0059D * j + 0.00639D * i)) * Math.cos(position + 0.3D * i + 0.3D * j));
					yPos[i][j] = (int) (patternHeight + 15 * j - 20.0D * Math.cos(position * (1.0D - 0.073D * j + 0.008489999999999999D * i)) * Math.sin(position + 0.23D * j + 0.389D * i));
				}
				if (kind == 2) {
					xPos[i][j] = (int) (centerX + 14.0D * i * Math.cos(0.01D * (40.0D - i) * position + j));
					yPos[i][j] = (int) (centerY - 14.0D * i * Math.sin(0.01D * (40.0D - i) * position + j));
				}
				if (kind == 3) {
					double d1 = 0.0D;
					int k = 0;
					if (i >= 0 && i < 2) {
						d1 = 1.0D;
						k = 0;
					}
					if (i >= 2 && i < 5) {
						d1 = -1.0D;
						k = 1;
					}
					if (i >= 5 && i < 8) {
						d1 = 1.0D;
						k = 2;
					}
					if (i >= 8 && i < 12) {
						d1 = -1.0D;
						k = 3;
					}
					if (i >= 12 && i < 17) {
						d1 = 1.0D;
						k = 4;
					}
					if (i >= 17 && i < 23) {
						d1 = -1.0D;
						k = 5;
					}
					if (i >= 23 && i < 31) {
						d1 = 1.0D;
						k = 6;
					}
					xPos[i][j] = (int) (centerX + 20.0D * (k + 4) * Math.cos(0.1D * position * d1 + j + 0.786D * i));
					yPos[i][j] = (int) (centerY - 20.0D * (k + 4) * Math.sin(0.1D * position * d1 + j + 0.786D * i));
				}
			}
		}

		// Draw points
		for (int i = 0; i < COLS; ++i) {
			for (int j = 0; j < LINES; ++j) {
				GCTemp.drawPoint(xPos[i][j], yPos[i][j]);
				GCTemp.drawPoint(xPos[i][j] + 1, yPos[i][j]);
				GCTemp.drawPoint(xPos[i][j], yPos[i][j] + 1);
				GCTemp.drawPoint(xPos[i][j] + 1, yPos[i][j] + 1);
			}
		}

		GCTemp.drawString("Click to change the effect (Here is effect #" + (kind + 1) + ")", 10, 10);

		GCTemp.dispose();
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

		final WaveEffect app = new WaveEffect(display);

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
