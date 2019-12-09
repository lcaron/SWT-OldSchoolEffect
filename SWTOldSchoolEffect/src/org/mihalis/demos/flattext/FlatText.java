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
package org.mihalis.demos.flattext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class FlatText {

	// Title
	private static final String SHELL_TITLE = "FlatText";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 400;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	//
	private int[] tex;
	private int[] sine;
	private int[] cose;
	private double ang;
	private int xd;
	private int yd;
	private static final int HAUTEUR = 100; // max 325

	public FlatText(Display display) {
		this.display = display;
	}

	public void init() {

		// Load texture
		final ImageData texture = new ImageData(getClass().getResourceAsStream("TEXFLAT2.png"));
		imageData = new ImageData(w, h, texture.depth, texture.palette);

		tex = new int[65536];
		texture.getPixels(0, 0, 65536, tex, 0);

		// Precompute sin & cos
		sine = new int[256];
		cose = new int[256];
		for (int i = 0; i < 256; i++) {
			sine[i] = (int) (Math.sin(i * 0.02454) * 256);
			cose[i] = (int) (Math.cos(i * 0.02454) * 256);
		}

		xd = 0;
		yd = 0;

	}

	public void animate() {

		final int a = (int) (HAUTEUR * 0.625);
		final int b = HAUTEUR * 100;

		ang += 2f;
		yd += 5;

		final int angnew = (int) (ang - ((int) (ang / 256) << 8));
		int u, v, index, unew, vnew;
		final int halfH = h / 2;
		final int halfW = w / 2;

		for (int y = 0; y < halfH; y++) {
			for (int x = 0; x < w; x++) {

				u = a * (x - halfW) / (y - halfH);
				v = b / (y - halfH);

				unew = (u * cose[angnew] - v * sine[angnew] >> 8) + xd;
				vnew = (u * sine[angnew] + v * cose[angnew] >> 8) + yd;

				index = unew + (vnew << 8) & 65535; // Eqv % 65536
				imageData.setPixel(x, h - y - 1, tex[index]);
			}
		}

		if (!canvas.isDisposed()) {
			canvas.redraw();
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

		final FlatText app = new FlatText(display);

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
