/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 * 
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * W.P. van Paassen - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.blob;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Blob {

	private static final String SHELL_TITLE = "Blob";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 800;
	private static final int CANVAS_HEIGHT = 600;

	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private int w, h;
	private ImageData imageData;

	private static final int BLOB_RADIUS = 44;
	private static final int BLOB_DRADIUS = BLOB_RADIUS * 2;
	private static final int BLOB_SRADIUS = BLOB_RADIUS * BLOB_RADIUS;
	private static final int NUMBER_OF_BLOBS = 20;
	private int[][] blob;
	private Point[] blobs;

	public Blob(Display display) {
		this.display = display;
	}

	public void init() {
		blob = new int[BLOB_DRADIUS][BLOB_DRADIUS];
		blobs = new Point[NUMBER_OF_BLOBS];

		/* create a suitable palette, this is crucial for a good effect */
		final RGB[] colors = new RGB[256];
		for (int i = 0; i < 256; ++i) {
			colors[i] = new RGB(i, i, i);
		}

		/* create blob */
		for (int i = -BLOB_RADIUS; i < BLOB_RADIUS; ++i) {
			for (int j = -BLOB_RADIUS; j < BLOB_RADIUS; ++j) {
				final int distance_squared = i * i + j * j;
				if (distance_squared <= BLOB_SRADIUS) {
					/* compute density */
					final float fraction = (float) distance_squared / (float) BLOB_SRADIUS;
					blob[i + BLOB_RADIUS][j + BLOB_RADIUS] = (int) (Math.pow(1.0 - fraction * fraction, 4.0) * 255.0);
				} else {
					blob[i + BLOB_RADIUS][j + BLOB_RADIUS] = 0;
				}
			}
		}

		for (int i = 0; i < NUMBER_OF_BLOBS; i++) {
			blobs[i] = new Point((w >> 1) - BLOB_RADIUS, (h >> 1) - BLOB_RADIUS);
		}

		imageData = new ImageData(w, h, 8, new PaletteData(colors));
	}

	public void animate() {

		/* move and draw blobs to screen */

		for (int i = 0; i < NUMBER_OF_BLOBS; i++) {
			blobs[i].x += -2 + (int) (5.0 * Math.random());
			blobs[i].y += -2 + (int) (5.0 * Math.random());
		}

		for (int k = 0; k < NUMBER_OF_BLOBS; ++k) {
			if (blobs[k].x > 0 && blobs[k].x < w - BLOB_DRADIUS && blobs[k].y > 0 && blobs[k].y < h - BLOB_DRADIUS) {
				int start = blobs[k].x + blobs[k].y * w;
				for (int i = 0; i < BLOB_DRADIUS; ++i) {
					for (int j = 0; j < BLOB_DRADIUS; ++j) {
						if (getPixel(start + j) + blob[i][j] > 255) {
							setPixel(start + j, 255);
						} else {
							setPixel(start + j, getPixel(start + j) + blob[i][j]);
						}
					}
					start += w;
				}
			} else {
				blobs[k].x = (w >> 1) - BLOB_RADIUS;
				blobs[k].y = (h >> 1) - BLOB_RADIUS;
			}
		}
		if (!canvas.isDisposed())
		canvas.redraw();
	}

	private void setPixel(int i, int value) {
		imageData.setPixel(i % w, i / w, value);
	}

	private int getPixel(int i) {
		return imageData.getPixel(i % w, i / w);
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(SHELL_TITLE);
		shell.setLayout(new GridLayout(1, false));

		canvas = new Canvas(shell, SWT.BORDER | SWT.NO_BACKGROUND);
		final GridData gdCanvas = new GridData(GridData.FILL, GridData.FILL, true, true);
		gdCanvas.widthHint = CANVAS_WIDTH;
		gdCanvas.heightHint = CANVAS_HEIGHT;
		canvas.setLayoutData(gdCanvas);

		canvas.addListener(SWT.Resize, e -> {
			w = canvas.getClientArea().width;
			h = canvas.getClientArea().height;
			init();
		});

		canvas.addListener(SWT.Paint, e -> {
			redrawCanvas(e.gc);
		});

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

		final Blob app = new Blob(display);

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
