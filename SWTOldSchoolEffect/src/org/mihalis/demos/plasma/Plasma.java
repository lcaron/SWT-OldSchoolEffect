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
package org.mihalis.demos.plasma;

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

public class Plasma {

	// Title
	private static final String SHELL_TITLE = "Plasma";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 480;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;
	private int[] palette;
	private final int sin[] = new int[1800];
	private int plasmaIndex = 1;

	public Plasma(Display display) {
		this.display = display;
	}

	public void init() {
		createColors();
		precomputeSin();
		imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
		redrawCanvas();
	}

	private void createColors() {
		palette = new int[256];
		int i, r = 0, g = 0, b = 0;
		for (i = 0; i < 256; i++) {
			palette[i] = createColor(0, 0, 0);
		}

		for (i = 0; i < 42; i++) {
			palette[i] = createColor(r * 4, g * 4, b * 4);
			r++;
		}

		for (i = 42; i < 84; i++) {
			palette[i] = createColor(r * 4, g * 4, b * 4);
			g++;
		}

		for (i = 84; i < 126; i++) {
			palette[i] = createColor(r * 4, g * 4, b * 4);
			b++;
		}

		for (i = 126; i < 168; i++) {
			palette[i] = createColor(r * 4, g * 4, b * 4);
			r--;
		}

		for (i = 168; i < 210; i++) {
			palette[i] = createColor(r * 4, g * 4, b * 4);
			g--;
		}

		for (i = 210; i < 252; i++) {
			palette[i] = createColor(r * 4, g * 4, b * 4);
			b--;
		}

	}

	private int createColor(int r, int g, int b) {
		return r << 16 | g << 8 | b;
	}

	private void precomputeSin() {
		for (int i = 0; i < 1800; i++) {
			sin[i] = (int) (Math.cos(Math.PI * i / 180) * 1024);
		}
	}

	public void animate() {
		drawPlasma();
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private void drawPlasma() {
		plasmaIndex += 2;

		if (plasmaIndex > 360) {
			plasmaIndex = 0;
		}

		for (int x = 0; x < CANVAS_WIDTH; x++) {
			final int indexX = 75 + (sin[(x << 1) + (plasmaIndex >> 1)] + sin[x + (plasmaIndex << 1)]
					+ (sin[(x >> 1) + plasmaIndex] << 1) >> 6);

			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				final int indexY = 75 + ((sin[y + (plasmaIndex << 1)] << 1) + sin[(y << 1) + (plasmaIndex >> 1)]
						+ (sin[y + plasmaIndex] << 1) >> 5);
				final int colorIndex = Math.abs((indexX * indexY >> 5) % 256);
				imageData.setPixel(x, y, palette[colorIndex]);
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

		final Plasma app = new Plasma(display);

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
