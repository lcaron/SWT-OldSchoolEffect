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
package org.mihalis.demos.fire;

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

public class Fire {

	private final Display display;
	private Canvas canvas;
	private int w, h;
	private ImageData imageData;
	private int[] palette;
	private int[][] fire;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	public Fire(Display display) {
		this.display = display;
	}

	public void init() {
		palette = new int[256];
		fire = new int[w][h];

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				fire[x][y] = 0;
			}
		}

		// generate the palette
		for (int x = 0; x < 256; x++) {
			// HSLtoRGB is used to generate colors:
			// Hue goes from 0 to 85: red to yellow
			// Saturation is always the maximum: 255
			// Lightness is 0..255 for x=0..128, and 255 for x=128..255
			palette[x] = HSLtoRGB(x / 3, 255, Math.min(255, x * 2));
		}

		imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
	}

	public void animate() {

		// randomize the bottom row of the fire buffer
		for (int x = 0; x < w; x++) {
			fire[x][h - 1] = (int) (Math.abs(32768 + Math.random() * 100) % 256);
		}
		// do the fire calculations for every pixel, from top to bottom
		for (int y = 0; y < h - 1; y++) {
			for (int x = 0; x < w; x++) {
				fire[x][y] = (fire[(x - 1 + w) % w][(y + 1) % h] + fire[x % w][(y + 1) % h]
						+ fire[(x + 1) % w][(y + 1) % h] + fire[x % w][(y + 2) % h]) * 32 / 129;
			}
		}

		// set the drawing buffer to the fire buffer, using the palette colors
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				imageData.setPixel(x, y, palette[fire[x][y]]);
			}

		}
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private int HSLtoRGB(float _h, float _s, float _l) {
		float r, g, b, h, s, l; // this function works with floats between 0 and 1
		float temp1, temp2, tempr, tempg, tempb;
		h = _h / 256.0f;
		s = _s / 256.0f;
		l = _l / 256.0f;

		// If saturation is 0, the color is a shade of gray
		if (s == 0) {
			r = g = b = l;
		} else {
			// If saturation > 0, more complex calculations are needed
			// Set the temporary values
			if (l < 0.5) {
				temp2 = l * (1 + s);
			} else {
				temp2 = l + s - l * s;
			}
			temp1 = 2 * l - temp2;
			tempr = (float) (h + 1.0 / 3.0);
			if (tempr > 1) {
				tempr--;
			}
			tempg = h;
			tempb = (float) (h - 1.0 / 3.0);
			if (tempb < 0) {
				tempb++;
			}

			// Red
			if (tempr < 1.0 / 6.0) {
				r = (float) (temp1 + (temp2 - temp1) * 6.0 * tempr);
			} else if (tempr < 0.5) {
				r = temp2;
			} else if (tempr < 2.0 / 3.0) {
				r = (float) (temp1 + (temp2 - temp1) * (2.0 / 3.0 - tempr) * 6.0);
			} else {
				r = temp1;
			}

			// Green
			if (tempg < 1.0 / 6.0) {
				g = (float) (temp1 + (temp2 - temp1) * 6.0 * tempg);
			} else if (tempg < 0.5) {
				g = temp2;
			} else if (tempg < 2.0 / 3.0) {
				g = (float) (temp1 + (temp2 - temp1) * (2.0 / 3.0 - tempg) * 6.0);
			} else {
				g = temp1;
			}

			// Blue
			if (tempb < 1.0 / 6.0) {
				b = (float) (temp1 + (temp2 - temp1) * 6.0 * tempb);
			} else if (tempb < 0.5) {
				b = temp2;
			} else if (tempb < 2.0 / 3.0) {
				b = (float) (temp1 + (temp2 - temp1) * (2.0 / 3.0 - tempb) * 6.0);
			} else {
				b = temp1;
			}
		}

		final int ri = Math.min(255, Math.round(r * 255f));
		final int gi = Math.min(255, Math.round(g * 255f));
		final int bi = Math.min(255, Math.round(b * 255f));
		return ri << 16 | gi << 8 | bi;
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("Fire Effect");
		shell.setLayout(new GridLayout(1, false));

		canvas = new Canvas(shell, SWT.BORDER | SWT.NO_REDRAW_RESIZE);
		final GridData gdCanvas = new GridData(GridData.FILL, GridData.FILL, true, true);
		gdCanvas.widthHint = 256;
		gdCanvas.heightHint = 256;
		canvas.setLayoutData(gdCanvas);

		canvas.addListener(SWT.Resize, e -> {
			w = canvas.getClientArea().width;
			h = canvas.getClientArea().height;
			init();
		});

		canvas.addPaintListener(e -> {
			redrawCanvas(e.gc);
		});

		return shell;
	}

	private void redrawCanvas(GC gc) {
		if (imageData == null) {
			return;
		}
		final Image img = new Image(display, imageData);
		if (img != null) {
			gc.drawImage(img, 0, 0);
		}
		img.dispose();

	}

	public static void main(String[] args) {
		final Display display = new Display();

		final Fire app = new Fire(display);

		final Shell shell = app.createWindow();
		shell.pack();
		shell.open();

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
