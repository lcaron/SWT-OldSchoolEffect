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
package org.mihalis.demos.burningsea;

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

/**
 * Original from Josh83
 */
public class BurningSea {

	// Title
	private static final String TITLE = "BurningSea";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 200;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private int w, h;
	private ImageData imageData;

	//
	int[] sinp;
	int[] ran1;
	int[] xfall, yfall, sfall;
	long ti;
	short t;
	int fall;
	int r1;
	static final int YSCROLL = 10;

	public BurningSea(Display display) {
		this.display = display;
	}

	public void init() {

		sinp = new int[628];
		for (int i = 0; i < 628; i++) {
			sinp[i] = (int) (Math.sin(i * 0.01) * 128);
		}

		ran1 = new int[10000];
		for (int i = 0; i < 10000; i++) {
			ran1[i] = (int) (Math.random() * 3);
		}

		// Palette
		final RGB[] colors = new RGB[256];
		int r = 0, g = 0, b = 0;
		for (int i = 0; i < 256; i++) {
			colors[i] = new RGB(0, 0, 0);
		}

		for (int i = 0; i < 64; i++) {
			colors[i] = new RGB(r, 0, 0);
			r++;
		}
		for (int i = 64; i < 128; i++) {
			colors[i] = new RGB(63, g, 0);
			g++;
		}
		for (int i = 128; i < 192; i++) {
			colors[i] = new RGB(63, 63, b);
			b++;
		}
		b = 0;
		for (int i = 192; i < 256; i++) {
			colors[i] = new RGB(0, 0, b);
			b++;
		}

		// Increase lighten
		for (int i = 1; i < 256; i++) {
			final RGB color = colors[i];
			final float[] temp = color.getHSB();
			colors[i] = new RGB(temp[0], temp[1], temp[2] * 4f);
		}

		xfall = new int[10];
		yfall = new int[10];
		sfall = new int[10];
		for (int x = 0; x < 10; x++) {
			xfall[x] = (int) (Math.random() * 300);
			yfall[x] = (int) (Math.random() * 50);
			sfall[x] = (int) (Math.random() * 4) + 1;
		}

		imageData = new ImageData(w, h, 8, new PaletteData(colors));

		ti = 0;
		t = 0;
		fall = 0;
		r1 = 0;
	}

	public void animate() {

		ti++;
		t += 8;
		if (t > 60000) {
			t = 0;
		}

		fall++;
		if (fall > 130) {
			fall = 0;
		}

		for (int x = 0; x < w; x++) {
			for (int y = 130; y < 133; y++) {
				imageData.setPixel(x, y, (int) (Math.random() * 100 + 80));
			}
		}

		for (int i = 0; i < 10; i++) {
			yfall[i] += sfall[i];
			if (yfall[i] > 125) {
				xfall[i] = (int) (Math.random() * 300);
				yfall[i] = 1;
				sfall[i] = (int) (Math.random() * 4 + 1);
			}

			for (int x = 0; x < sfall[i] + 2; x++) {
				for (int y = 0; y < sfall[i] + 2; y++) {
					imageData.setPixel(x + xfall[i], y + yfall[i], (int) (Math.random() * 50 + (sfall[i] << 5)));
				}
			}
		}

		for (int ab = w - 2; ab > 0; ab--) {
			for (int bb = 131; bb > 0; bb--) {
				if (bb < YSCROLL - 1 || bb > YSCROLL + 12) {
					if (imageData.getPixel(ab, bb + 1) > 0 || imageData.getPixel(ab, bb) > 0) {
						r1++;
						if (r1 > 9000) {
							r1 = 0;
						}
						int cblur = imageData.getPixel(ab - 1, bb + 1) + imageData.getPixel(ab + 1, bb + 1)
								+ ran1[r1] * imageData.getPixel(ab, bb + 1) + imageData.getPixel(ab, bb) >> 2;
						if (cblur > 190) {
							cblur = 190;
						}
						imageData.setPixel(ab, bb, cblur);
					}
				}
				if (imageData.getPixel(ab, bb) > 0) {
					imageData.setPixel(ab, bb, imageData.getPixel(ab, bb) - 1);
				}

				final int abnew = ab + (sinp[(t + bb * 20) % 628] * (bb - 110 >> 2) >> 7);
				final int bbnew = 265 - bb;

				if (abnew > 0 && abnew < w) {
					if (bbnew > 0 && bbnew < h) {
						imageData.setPixel(ab, bbnew, imageData.getPixel(abnew, bb) >> 1);
					}
				}
			}
		}

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < 30; y++) {
				imageData.setPixel(x, y, 0);
			}
		}

		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(TITLE);
		shell.setLayout(new GridLayout(1, false));

		canvas = new Canvas(shell, SWT.BORDER | SWT.NO_REDRAW_RESIZE);
		final GridData gdCanvas = new GridData(GridData.FILL, GridData.FILL, true, true);
		gdCanvas.widthHint = CANVAS_WIDTH;
		gdCanvas.heightHint = CANVAS_HEIGHT;
		canvas.setLayoutData(gdCanvas);

		canvas.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event e) {
				w = canvas.getClientArea().width;
				h = canvas.getClientArea().height;
				init();
			}
		});

		canvas.addListener(SWT.Paint, e -> redrawCanvas(e.gc));

		return shell;
	}

	private void redrawCanvas(GC gc) {
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

		final BurningSea app = new BurningSea(display);

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
