/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Laurent CARON (laurent.caron at gmail dot com) - Original Version
 *******************************************************************************/
package org.mihalis.demos.moire;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Moire {

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;
	private static final int STEP = 12;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private Image image;
	private Image circle;
	private Image bigCircle;
	private float t;

	public Moire(Display display) {
		this.display = display;
	}

	public void init() {
		image = new Image(display, w, h);
		final GC tempGC = new GC(image);
		tempGC.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		tempGC.fillRectangle(0, 0, w, h);
		tempGC.dispose();

		circle = createCircles(w / 2, h / 2, w, h);
		final Image temp = createCircles(w, h, w * 4, h * 3);
		final ImageData data = temp.getImageData();
		data.transparentPixel = data.palette.getPixel(new RGB(255, 255, 255));
		bigCircle = new Image(display, data);

		redrawCanvas();
	}

	public void animate() {
		t += 0.02;
		final double posX = 100 + 50 * Math.cos(t) + 50 * Math.cos(3 * t) - w;
		final double posY = 100 + 50 * Math.sin(t) + 40 * Math.sin(Math.sqrt(2) * t) - h;

		final GC tempGC = new GC(image);
		tempGC.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		tempGC.fillRectangle(0, 0, w, h);

		tempGC.drawImage(circle, 0, 0);
		tempGC.drawImage(bigCircle, (int) posX, (int) posY);

		tempGC.dispose();
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private Image createCircles(int centerX, int centerY, int width, int height) {
		final Image img = new Image(display, width, height);
		final GC tempGC = new GC(img);
		tempGC.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		tempGC.setLineWidth(3);
		tempGC.setAntialias(SWT.ON);

		for (int i = 0; i < Math.max(width, height) * 2; i += STEP) {
			tempGC.drawOval(centerX - i / 2, centerY - i / 2, i, i);
		}

		tempGC.dispose();
		return img;
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("Moire");
		shell.setLayout(new GridLayout(1, false));

		canvas = new Canvas(shell, SWT.BORDER | SWT.NO_REDRAW_RESIZE);
		final GridData gdCanvas = new GridData(GridData.FILL, GridData.FILL, true, true);
		gdCanvas.widthHint = 256;
		gdCanvas.heightHint = 256;
		canvas.setLayoutData(gdCanvas);

		gc = new GC(canvas);

		canvas.addListener(SWT.Resize, e -> {
			w = canvas.getClientArea().width;
			h = canvas.getClientArea().height;
			init();
		});

		canvas.addPaintListener(e -> {
			gc = e.gc;
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

		final Moire app = new Moire(display);

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
