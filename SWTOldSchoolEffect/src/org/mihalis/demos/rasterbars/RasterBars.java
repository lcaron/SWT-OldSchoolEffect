/*******************************************************************************
 * Copyright (c) 2019 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Copyright (C) Calogiuri Enzo Antonio 2011 <Insolit Dust - http://insolitdust.sourceforge.net/code.html> - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.rasterbars;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RasterBars {

	// Title
	private static final String SHELL_TITLE = "Rasters";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 320;
	private static final int CANVAS_HEIGHT = 240;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;
	private int[] palette;

	// Sin offset for rasterbar
	private int offset1, offset2;

	public RasterBars(Display display) {
		this.display = display;
	}

	public void init() {
		createColors();
		imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
	}

	private void createColors() {
		palette = new int[256];
		// 80 color for first rasterbar
		for (int i = 0; i < 80; i++) {
			palette[i] = createColor(i, i * 2, i * 3);
		}

		// 80 color for second raterbar
		for (int i = 80; i < 160; i++) {
			palette[i] = createColor(i - 80, (i - 80) * 3, (i - 80) * 2);
		}

		// Grayscale palette for background
		for (int i = 160; i < 256; i++) {
			palette[i] = createColor(i - 160, i - 160, i - 160);
		}

	}

	private int createColor(int r, int g, int b) {
		return r << 16 | g << 8 | b;
	}

	public void animate() {
		drawBackground();
		drawBars();
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
		offset1 += 2;
		offset2 += 2;
	}

	private void drawBackground() {
		float color = 160f;
		final float inc = 96f / CANVAS_HEIGHT;

		for (int a = 0; a < CANVAS_HEIGHT; a++) {
			drawLine(0, a, CANVAS_WIDTH, a, (int) color);
			color += inc;
		}
	}

	private void drawLine(int x1, int y1, int x2, int y2, int color) {

		final int dx = Math.abs(x1 - x2);
		final int dy = Math.abs(y1 - y2);
		int cxy = 0;
		int dxy;

		if (dy > dx) {
			if (y1 > y2) {
				// swap
				int temp = y2;
				y2 = y1;
				y2 = temp;

				temp = x2;
				x2 = x1;
				x2 = temp;
			}

			if (x1 > x2) {
				dxy = -1;
			} else {
				dxy = 1;
			}

			for (int y = y1; y < y2; y++) {
				cxy += dx;

				if (cxy >= dy) {
					x1 += dxy;
					cxy -= dy;
				}

				imageData.setPixel(x1, y, palette[color]);
			}
		} else {
			if (x1 > x2) {
				// swap
				int temp = y2;
				y2 = y1;
				y2 = temp;

				temp = x2;
				x2 = x1;
				x2 = temp;
			}

			if (y1 > y2) {
				dxy = -1;
			} else {
				dxy = 1;
			}

			for (int x = x1; x < x2; x++) {
				cxy += dy;

				if (cxy >= dx) {
					y1 += dxy;
					cxy -= dx;
				}

				imageData.setPixel(x, y1, palette[color]);
			}
		}
	}

	private void drawBars() {
		int color = 0;
		int a, c, d;
		for (c = 0; c < CANVAS_HEIGHT; c += 3) {
			d = (int) (150 + 60 * Math.sin((c + offset1) * 3.14 / 180) + 50 * Math.cos((c + offset2) * 4.14 / 180));

			for (a = d; a < d + 20; a++) {
				drawLine(a, c, a, CANVAS_HEIGHT, color);
			}

			d = (int) (145 + 100 * Math.sin((c + offset2) * 4.14 / 180) + 50 * Math.cos((c + offset2) * 4.14 / 180));

			for (a = d; a < d + 35; a++) {
				drawLine(a, c + 3, a, CANVAS_HEIGHT, color + 80);
			}

			color++;
		}
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(SHELL_TITLE);
		shell.setLayout(new GridLayout(1, false));

		canvas = new Canvas(shell, SWT.BORDER | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED);
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

		final RasterBars app = new RasterBars(display);

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
