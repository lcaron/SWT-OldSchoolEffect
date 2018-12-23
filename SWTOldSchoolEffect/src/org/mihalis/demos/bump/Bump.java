/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Laurent Lepinay - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.bump;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Bump {

	private static final String SHELL_TITLE = "Bump";

	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 400;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 1;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	//
	private ImageData image;
	private int[] nEnvMap;
	private float fTime;
	private int lX, lY;

	public Bump(final Display display) {
		this.display = display;
	}

	public void init() {

		// Create environment map
		nEnvMap = new int[256 * 256];
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++) {
				nEnvMap[x + 256 * y] = (int) (255 - 255 * Math.sqrt((x - 128) * (x - 128) + (y - 128) * (y - 128)) / Math.sqrt(128 * 128 + 128 * 128));
			}
		}

		// Create Palette
		final RGB[] colors = new RGB[256];
		for (int i = 0; i < 64; i++) {
			colors[i] = new RGB(0, 0, 0);
		}
		for (int i = 64; i < 128; i++) {
			colors[i] = new RGB(0, 0, (i - 64) * 4);
		}
		for (int i = 128; i < 256; i++) {
			colors[i] = new RGB((i - 128) * 2, (i - 128) * 2, 255);
		}

		// Load image
		image = new ImageData(getClass().getResourceAsStream("bump.png"));

		// Init
		fTime = 0.0f;
		lX = (int) (CANVAS_WIDTH / 2 + 80 * Math.cos(fTime += .1));
		lY = (int) (CANVAS_HEIGHT / 2 + 80 * Math.sin(fTime));

		imageData = new ImageData(w, h, 8, new PaletteData(colors));

		redrawCanvas();
	}

	public void animate() {

		for (int y = 1; y < CANVAS_HEIGHT - 1; y++) {
			for (int x = 1; x < CANVAS_WIDTH - 1; x++) {
				// Pixel orientation
				int dX = (image.getPixel(x + 1, y) >> 16) - (image.getPixel(x - 1, y) >> 16);
				int dY = (image.getPixel(x, y + 1) >> 16) - (image.getPixel(x, y - 1) >> 16);

				// Retrieve the illumation level
				dX = dX - (lX - x);
				dY = dY - (lY - y);
				if (dX <= -128 || dX >= 128) {
					dX = dY = -128;
				}
				if (dY <= -128 || dY >= 128) {
					dX = dY = -128;
				}
				dX += 128;
				dY += 128;

				// Put pixel
				imageData.setPixel(x, y, nEnvMap[dX + 256 * dY]);
			}
		}

		lX = (int) (CANVAS_WIDTH / 2 + 80 * Math.cos(fTime += .1));
		lY = (int) (CANVAS_HEIGHT / 2 + 80 * Math.sin(fTime));

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

		canvas.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				w = canvas.getClientArea().width;
				h = canvas.getClientArea().height;
				init();
				canvas.removeListener(SWT.Resize, this);
			}
		});

		canvas.addPaintListener(e -> {
			redrawCanvas();
		});

		return shell;
	}

	private void redrawCanvas() {
		if (imageData == null) {
			return;
		}
		final Image image = new Image(display, imageData);
		if (image != null) {
			gc.drawImage(image, 0, 0);
		}
		image.dispose();
	}

	public static void main(final String[] args) {
		final Display display = new Display();

		final Bump app = new Bump(display);

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
