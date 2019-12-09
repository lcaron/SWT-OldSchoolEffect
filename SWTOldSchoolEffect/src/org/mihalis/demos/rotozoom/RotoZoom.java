/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * W.P. van Paassen & Byron Ellacott - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.rotozoom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RotoZoom {

	// Title
	private static final String SHELL_TITLE = "RotoZoom";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 480;
	private static final int CANVAS_HEIGHT = 360;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	//
	private ImageData tile;
	private int[] roto;
	private int[] roto2;
	private int path, zpath;

	public RotoZoom(Display display) {
		this.display = display;
	}

	public void init() {

		// Load image
		tile = new ImageData(getClass().getResourceAsStream("tux256256.png"));
		roto = new int[256];
		roto2 = new int[256];

		for (int i = 0; i < 256; i++) {
			final float rad = (float) (i * 1.41176 * 0.0174532);
			final float c = (float) Math.sin(rad);
			roto[i] = (int) ((c + 0.8) * 4096.0);
			roto2[i] = (int) (2.0 * c * 4096.0);
		}

		path = 0;
		zpath = 0;

		imageData = new ImageData(w, h, 24, tile.palette);

	}

	public void animate() {
		// Animation

		/* draw the tile at current zoom and rotation */
		draw_tile(roto[path], roto[path + 128 & 255], roto2[zpath]);
		path = path - 1 & 255;
		zpath = zpath + 1 & 255;
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private void draw_tile(int stepx, int stepy, int zoom) {
		int x, y, i, j, xd, yd, a, b, sx, sy;

		sx = sy = 0;
		xd = stepx * zoom >> 12;
		yd = stepy * zoom >> 12;

		/*
		 * Stepping across and down the screen, each screen row has a starting
		 * coordinate in the texture: (sx, sy). As each screen row is traversed, the
		 * current texture coordinate (x, y) is modified by (xd, yd), which are
		 * (sin(rot), cos(rot)) multiplied by the current zoom factor. For each vertical
		 * step, (xd, yd) is rotated 90 degrees, to become (-yd, xd).
		 *
		 * More fun can be had by playing around with x, y, xd, and yd as you move about
		 * the image.
		 */

		int index = 0;
		for (j = 0; j < h; j++) {
			x = sx;
			y = sy;
			for (i = 0; i < w; i++) {
				a = x >> 12 & 255;
				b = y >> 12 & 255;
				imageData.setPixel(index % w, index / w, tile.getPixel(a, b));
				index++;

				x += xd;
				y += yd;
			}
			sx -= yd;
			sy += xd;
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

		final RotoZoom app = new RotoZoom(display);

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
