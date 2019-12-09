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
package org.mihalis.demos.wobble;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class WobbleEffect {

	// Title
	private static final String TITLE = "WobbleEffect";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 210;
	private static final int CANVAS_HEIGHT = 200;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private Image image;

	//
	private ImageData img;
	double dk;
	double aci;
	double prm1;
	double prm2;
	double prm3;
	boolean direction;
	int rot_angle;
	int[] dt;
	int[] isle;
	int[] isle2;
	int cx;
	int cy;
	int icx;
	int icy;

	private int imgh;

	private int imgw;

	public WobbleEffect(Display display) {
		this.display = display;
	}

	public void init() {

		// Load images
		img = new ImageData(getClass().getResourceAsStream("bu.jpg"));

		final ImageData imageData = new ImageData(w, h, img.depth, img.palette);
		image = new Image(display, imageData);

		dk = 0.0D;
		prm1 = 6 / 10.0D;
		prm2 = 20 / 10.0D;
		prm3 = 5 / 10.0D;
		direction = true;
		rot_angle = 2;

		imgw = img.width;
		imgh = img.height;

		dt = new int[imgw * imgh];
		isle = new int[w * h];
		isle2 = new int[w * h];

		img.getPixels(0, 0, imgw * imgh, dt, 0);

		icx = (int) Math.floor(imgw / 2);
		icy = (int) Math.floor(imgh / 2);

		cx = (int) Math.floor(w / 2);
		cy = (int) Math.floor(h / 2);

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (i > cx - icx && i < cx + icx && j > cy - icy && j < cy + icy) {
					isle2[j * w + i] = dt[i - cx + icx + (j - cy + icy) * imgw];
				} else {
					isle2[j * w + i] = 0xFFFFFFFF;
				}

			}

		}

		aci = 0.0D;

	}

	public void animate() {

		// Clear screen
		final GC GCTemp = new GC(image);
		GCTemp.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		GCTemp.fillRectangle(0, 0, w, h);
		GCTemp.dispose();
		final ImageData imageData = image.getImageData();

		final double b = Math.sin(aci);
		for (int i = 0; i < w; i++) {
			final double a = Math.cos(aci + Math.cos(3.141592653589793D / cx * prm1 * Math.abs(cx - i)));
			for (int j = 0; j < h; j++) {
				isle[j * w + i] = 0xFFFFFFFF;

				final int ix = cx + (int) Math.round((i - cx) * a * prm2 - (j - cy) * b);
				final int iy = cy + (int) Math.round((i - cx) * b * prm3 + (j - cy) * a);

				if (ix >= 0 && iy >= 0 && ix < w && iy < h) {
					isle[w * j + i] = isle2[w * iy + ix];
				}
			}

		}
		imageData.setPixels(0, 0, w * h, isle, 0);
		image = new Image(display, imageData);

		//
		if (direction) {
			dk += rot_angle;
		} else {
			dk -= rot_angle;
		}

		if (direction) {
			if (dk >= 360.0D) {
				dk = 0.0D;
			} else if (dk <= 0.0D) {
				dk = 360.0D;
			}
		}

		aci = dk * 0.0174532925199433D;
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(TITLE);
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
		if (image != null) {
			gc.drawImage(image, 0, 0);
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();

		final WobbleEffect app = new WobbleEffect(display);

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
