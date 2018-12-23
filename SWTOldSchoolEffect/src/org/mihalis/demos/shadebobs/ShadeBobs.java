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
package org.mihalis.demos.shadebobs;

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
import org.eclipse.swt.widgets.Shell;

public class ShadeBobs {

	// Title
	private static final String SHELL_TITLE = "ShadeBobs";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 480;
	private static final int CANVAS_HEIGHT = 360;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	private int xpath[];
	private int ypath[];
	private int pathpath[];

	/* the shadebob image to apply */
	private int heat[][] = { { 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0 }, //
			{ 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 }, //
			{ 0, 0, 0, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 0, 0, 0 }, //
			{ 0, 0, 1, 1, 2, 2, 2, 3, 3, 2, 2, 2, 1, 1, 0, 0 }, //
			{ 0, 0, 1, 2, 2, 3, 3, 3, 3, 3, 3, 2, 2, 1, 0, 0 }, //
			{ 0, 1, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 1, 0 }, //
			{ 0, 1, 2, 2, 3, 3, 3, 4, 4, 3, 3, 3, 2, 2, 1, 0 }, //
			{ 1, 1, 2, 3, 3, 3, 4, 4, 4, 4, 3, 3, 3, 2, 1, 1 }, //
			{ 1, 1, 2, 3, 3, 3, 4, 4, 4, 4, 3, 3, 3, 2, 1, 1 }, //
			{ 0, 1, 2, 2, 3, 3, 3, 4, 4, 3, 3, 3, 2, 2, 1, 0 }, //
			{ 0, 1, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 1, 0 }, //
			{ 0, 0, 1, 2, 2, 3, 3, 3, 3, 3, 3, 2, 2, 1, 0, 0 }, //
			{ 0, 0, 1, 1, 2, 2, 2, 3, 3, 2, 2, 2, 1, 1, 0, 0 }, //
			{ 0, 0, 0, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 0, 0, 0 }, //
			{ 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 }, //
			{ 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0 }, //
	};

	private int trail;

	public ShadeBobs(Display display) {
		this.display = display;
	}

	public void init() {

		// Init
		xpath = new int[512];
		ypath = new int[512];
		pathpath = new int[1024];

		final int hw = w - 150;
		final int hh = h - 180;
		final int aw = 67;
		final int ah = 82;

		/* initialise a movement path for the bob */
		for (int i = 0; i < 512; i++) {
			/* spread 360 degrees over 512 values and convert to rad */
			final double rad = i * 0.703125 * 0.0174532;
			xpath[i] = (int) (Math.sin(rad * 2) * hw / 2 + hw / 2 + aw);
			ypath[i] = (int) (Math.sin(rad) * hh / 2 + hh / 2 + ah);
		}

		for (int i = 0; i < 1024; i++) {
			/* spread 360 degrees over 1024 values and convert to rad */
			final double rad = i * 0.3515625 * 0.0174532;
			pathpath[i] = (int) (Math.sin(rad) * 15);
		}

		/* create a suitable shadebob palette, this is crucial for a good effect */
		/* black to blue, blue to red, red to white */
		final RGB[] colors = new RGB[256];
		for (int i = 0; i < 64; ++i) {
			colors[i] = new RGB(0, 0, i << 1);
			colors[i + 64] = new RGB(i << 1, 0, 128 - (i << 1));
			colors[i + 128] = new RGB(128 + (i << 1), 0, 128 - (i << 1));
			colors[i + 192] = new RGB(255, i << 2, i << 2);
		}

		imageData = new ImageData(w, h, 8, new PaletteData(colors));
		trail = 0;
		redrawCanvas();
	}

	public void animate() {
		// Animation

		int remx = 0, remy = 0;
		int drawx = 0, drawy = 0;
		int i, j, tmp;

		/* remove heat from tail */
		if (trail >= 500) {
			tmp = trail - 500;
			remx = get_bob_x_location(tmp);
			remy = get_bob_y_location(tmp);
			for (i = 0; i < 16; i++) {
				tmp = (remy + i) * w + remx;
				for (j = 0; j < 16; j++) {
					int val = imageData.getPixel((tmp + j) % w, (tmp + j) / w);
					val -= heat[i][j] * 8;
					if (val < 0) {
						val = 0;
					}
					imageData.setPixel((tmp + j) % w, (tmp + j) / w, val);
				}
			}
		}

		/* add heat at new head */
		drawx = get_bob_x_location(trail);
		drawy = get_bob_y_location(trail);
		for (i = 0; i < 16; i++) {
			tmp = (drawy + i) * w + drawx;
			for (j = 0; j < 16; j++) {
				int val = imageData.getPixel((tmp + j) % w, (tmp + j) / w);
				val += heat[i][j] * 8;
				if (val > 255) {
					val = 255;
				}
				imageData.setPixel((tmp + j) % w, (tmp + j) / w, val);
			}
		}
		trail++;

		redrawCanvas();
	}

	private int get_bob_x_location(int index) {
		return xpath[index & 511] + pathpath[index & 1023];
	}

	private int get_bob_y_location(int index) {
		return ypath[index & 511] + pathpath[index & 1023];
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

		final ShadeBobs app = new ShadeBobs(display);

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
