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
package org.mihalis.demos.unlimitedballs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class UnlimitedBalls {

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private Image[] offscreenImage;
	private Image ball;

	private int Xc, Yc;
	private float Xr, Yr;
	private int count;
	private int current;
	private int xspeed, yspeed, xrspeed, yrspeed, xstart, ystart, xrstart, yrstart;
	private int currentShape = 0;

	public UnlimitedBalls(Display display) {
		this.display = display;
	}

	public void init() {
		int i;
		count = 0;
		current = 0;
		Xc = w / 2 - 8;
		Yc = h / 2 - 8;
		Xr = w / 2.5F;
		Yr = h / 2.5F;

		offscreenImage = new Image[8];
		for (i = 0; i < 8; i++) {
			final Image image = new Image(display, w, h);
			final GC tempGC = new GC(image);
			tempGC.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			tempGC.fillRectangle(0, 0, w, h);
			tempGC.dispose();
			offscreenImage[i] = image;
		}

		ball = new Image(display, getClass().getResourceAsStream("ball.gif"));

		switch (currentShape) {
			case 0:
				// First set of parameter
				xspeed = 100;
				yspeed = 100;
				xrspeed = 4150;
				yrspeed = 4150;
				xstart = 0;
				ystart = 0;
				xrstart = 0;
				yrstart = 4150;
				break;
			case 1:
				// Second set of parameter
				xspeed = 199999;
				yspeed = 100;
				xrspeed = 550;
				yrspeed = 950;
				xstart = 0;
				ystart = 50;
				xrstart = 0;
				yrstart = 0;
				break;
			default:
				// Third set of parameter
				xspeed = 100;
				yspeed = 150;
				xrspeed = 2200;
				yrspeed = 1100;
				xstart = 0;
				ystart = 150;
				xrstart = 0;
				yrstart = 1100;
				break;
		}
	}

	public void animate() {
		int i;
		double x, y;
		double xr, yr;

		count = (count + 1) % 8;
		for (i = 0; i < 8; i++) {
			xr = Math.cos((current + xrstart) * Math.PI / xrspeed) * Xr;
			yr = Math.cos((current + yrstart) * Math.PI / yrspeed) * Yr;
			x = Math.cos((current + xstart) * Math.PI / xspeed) * xr;
			y = Math.sin((current + ystart) * Math.PI / yspeed) * yr;
			final GC tempGC = new GC(offscreenImage[i]);
			tempGC.drawImage(ball, (int) (Xc + Math.round(x)), (int) (Yc + Math.round(y)));
			tempGC.dispose();
			current++;
		}
		redrawCanvas();
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("Plasma");
		shell.setLayout(new GridLayout(1, false));

		canvas = new Canvas(shell, SWT.BORDER | SWT.NO_REDRAW_RESIZE);
		final GridData gdCanvas = new GridData(GridData.FILL, GridData.FILL, true, true);
		gdCanvas.widthHint = 512;
		gdCanvas.heightHint = 512;
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

		canvas.addListener(SWT.MouseUp, e -> {
			currentShape = (currentShape + 1) % 3;
			init();
		});

		return shell;
	}

	private void redrawCanvas() {
		if (offscreenImage == null) {
			return;
		}
		gc.drawImage(offscreenImage[count], 0, 0);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.drawText("Number of sprites :" + current, 5, 5);

		gc.drawText("Clic to change the shape", 5, h - 15);
	}

	public static void main(String[] args) {
		final Display display = new Display();

		final UnlimitedBalls app = new UnlimitedBalls(display);

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
