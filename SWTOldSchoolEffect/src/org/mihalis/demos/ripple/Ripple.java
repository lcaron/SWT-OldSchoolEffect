/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Neil Wallis (http://www.neilwallis.com/java/water.html) - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.ripple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Ripple {

	// Title
	private static final String SHELL_TITLE = "Ripple";

	private static final int CANVAS_WIDTH = 408;
	private static final int CANVAS_HEIGHT = 306;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 1000 / 30;

	private final Display display;
	private Canvas canvas;
	private GC gc;

	//
	private ImageData offImage;
	private int width;
	private int height;
	private int hwidth;
	private int hheight;
	private int riprad;
	private int size;
	private short[] ripplemap;
	private int[] ripple;
	private int[] texture;
	private int oldind;
	private int newind;
	private int mapind;

	public Ripple(Display display) {
		this.display = display;
	}

	public void init() {

		// Load image
		final ImageData img = new ImageData(getClass().getResourceAsStream("ocean.jpg"));

		width = img.width;
		height = img.height;
		hwidth = width >> 1;
		hheight = height >> 1;
		riprad = 3;

		size = width * (height + 2) * 2;
		ripplemap = new short[size];
		ripple = new int[width * height];
		texture = new int[width * height];
		oldind = width;
		newind = width * (height + 3);

		img.getPixels(0, 0, width * height, texture, 0);
		offImage = new ImageData(width, height, img.depth, img.palette);

		canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				disturb(e.x, e.y);
			}
		});
	}

	public void animate() {
		newframe();
		// Copie ripple dans offImage
		offImage.setPixels(0, 0, width * height, ripple, 0);
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private void newframe() {
		int i, a, b;
		// Toggle maps each frame
		i = oldind;
		oldind = newind;
		newind = i;

		i = 0;
		mapind = oldind;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				short data = (short) (ripplemap[mapind - width] + ripplemap[mapind + width] + ripplemap[mapind - 1]
						+ ripplemap[mapind + 1] >> 1);
				data -= ripplemap[newind + i];
				data -= data >> 5;
				ripplemap[newind + i] = data;

				// where data=0 then still, where data>0 then wave
				data = (short) (1024 - data);

				// offsets
				a = (x - hwidth) * data / 1024 + hwidth;
				b = (y - hheight) * data / 1024 + hheight;

				// bounds check
				if (a >= width) {
					a = width - 1;
				}
				if (a < 0) {
					a = 0;
				}
				if (b >= height) {
					b = height - 1;
				}
				if (b < 0) {
					b = 0;
				}

				ripple[i] = texture[a + b * width];
				mapind++;
				i++;
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
			init();
		});

		canvas.addPaintListener(e -> {
			gc = e.gc;
			redrawCanvas();
		});

		return shell;
	}

	private void redrawCanvas() {
		if (offImage == null) {
			return;
		}
		final Image image = new Image(display, offImage);
		if (image != null) {
			gc.drawImage(image, 0, 0);
		}
		image.dispose();
	}

	private void disturb(int dx, int dy) {
		for (int j = dy - riprad; j < dy + riprad; j++) {
			for (int k = dx - riprad; k < dx + riprad; k++) {
				if (j >= 0 && j < height && k >= 0 && k < width) {
					ripplemap[oldind + j * width + k] += 512;
				}
			}
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();

		final Ripple app = new Ripple(display);

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
