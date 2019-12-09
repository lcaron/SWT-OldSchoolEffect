/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Josh83 - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.sky2;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Sky2 {

	// Title
	private static final String SHELL_TITLE = "Sky2";

	private static final int CANVAS_WIDTH = 320;
	private static final int CANVAS_HEIGHT = 200;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	private ImageData sprite;
	private int t;

	public Sky2(Display display) {
		this.display = display;
	}

	public void init() {

		// Load image
		sprite = new ImageData(getClass().getResourceAsStream("DEMO2.png"));
		imageData = new ImageData(w, h, sprite.depth, sprite.palette);
		t = 0;
		redrawCanvas();
	}

	public void animate() {
		// Clear screen
		final Image tmp = new Image(display, imageData);
		final GC GCTemp = new GC(tmp);
		GCTemp.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
		GCTemp.fillRectangle(0, 0, w, h);
		GCTemp.dispose();
		tmp.dispose();

		// Go
		t += 10;

		// Textures
		for (int z = 1; z < 91; z++) {

			int u = 0;
			final int difx = 2 * z + 10;
			final int ustep = (160 << 7) / difx;

			for (int x = 0; x < 160; x++) {
				final int xnew = (u >> 7) - (u >> 14 << 7);
				final int ynew = 7000 / z + t - (7000 / z + t >> 7 << 7);
				final int xnew2 = (u >> 9) - (u >> 16 << 7);
				final int ynew2 = 2048 / z + (t >> 5) - (2048 / z + (t >> 5) >> 7 << 7);

				imageData.setPixel(160 + x, 90 - z, sprite.getPixel(255 - xnew2, ynew2));
				imageData.setPixel(160 - x, 90 - z, sprite.getPixel(128 + xnew2, ynew2));
				imageData.setPixel(160 + x, 91 + z, sprite.getPixel(127 - xnew, ynew));
				imageData.setPixel(160 - x, 91 + z, sprite.getPixel(xnew, ynew));

				u += ustep;
			}

		}

		for (int b = 0; b < 200; b += 10) {
			final int e = (int) (110 + 20 * Math.cos((b + t) * 0.01) + 15 * Math.sin((4 * b + t) * 0.01));
			final int f = (int) (160 + 50 * Math.sin((2 * b + t) * 0.01) + 25 * Math.cos((2 * b + t) * 0.01));
			Sprite(f, e, 127, 127, 0.1f, 0.1f);
			Shade(f, (e >> 2) + 135, 127, 127, 0.1f, 0.04f);
		}

		// Scrolling
		for (int d = 0; d < 320; d++) {
			final int t2 = t / 9 + d - (t / 9 + d >> 10 << 10);
			final int ys = t2 >> 7;
			final int xs = t2 - (ys << 7);

			for (int c = 0; c < 15; c++) {
				imageData.setPixel(d, c + 184, sprite.getPixel(xs + 128, ys * 16 + 128 + c));
			}
		}
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private void Shade(int x, int y, int xs, int ys, float rx, float ry) {
		int xn, yn;
		int an, bn;
		int rn, tn;
		xn = (int) (xs * rx);
		yn = (int) (ys * ry);
		if (xn < 2) {
			xn = 2;
		}
		if (yn < 2) {
			yn = 2;
		}
		an = (int) ((float) (ys << 8) / yn);
		bn = (int) ((float) (xs << 8) / xn);
		rn = x - (xn >> 1);
		tn = y - (yn >> 1);
		for (int ut = 1; ut < xn; ut++) {
			for (int vt = 1; vt < yn; vt++) {
				final int pixel = sprite.getPixel(ut * bn >> 8, (vt * an >> 8) + 128);
				if (pixel != 255) {
					imageData.setPixel(ut + rn, vt + tn, pixel);
				}
			}
		}
	}

	private void Sprite(int x, int y, int xs, int ys, float rx, float ry) {
		int xn, yn;
		int an, bn;
		int rn, tn;
		xn = (int) (xs * rx);
		yn = (int) (ys * ry);
		if (xn < 2) {
			xn = 2;
		}
		if (yn < 2) {
			yn = 2;
		}
		an = (int) ((float) (ys << 8) / yn);
		bn = (int) ((float) (xs << 8) / xn);
		rn = x - (xn >> 1);
		tn = y - (yn >> 1);
		for (int ut = 1; ut < xn; ut++) {
			for (int vt = 1; vt < yn; vt++) {
				final int pixel = sprite.getPixel(ut * bn >> 8, (vt * an >> 8) + 128);
				if (pixel != 255) {
					imageData.setPixel(ut + rn, vt + tn, pixel);
				}
			}
		}
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
			gc = e.gc;
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

	public static void main(String[] args) {
		final Display display = new Display();

		final Sky2 app = new Sky2(display);

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
