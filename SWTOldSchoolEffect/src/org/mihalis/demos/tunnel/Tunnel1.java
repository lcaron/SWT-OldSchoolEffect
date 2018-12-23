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
package org.mihalis.demos.tunnel;

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

public class Tunnel1 {

	private Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;
	private int[][] texture;
	private int[][] distanceTable;
	private int[][] angleTable;
	private float animation;

	// TEXTURE SIZE
	private static final int TEX_WIDTH = 256;
	private static final int TEX_HEIGHT = 256;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	public Tunnel1(Display display) {
		this.display = display;
	}

	public void init() {
		imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
		texture = new int[TEX_WIDTH][TEX_HEIGHT];
		distanceTable = new int[w][h];
		angleTable = new int[w][h];

		// generate texture
		for (int x = 0; x < TEX_WIDTH; x++) {
			for (int y = 0; y < TEX_HEIGHT; y++) {
				texture[x][y] = x * 256 / TEX_WIDTH ^ y * 256 / TEX_HEIGHT;
			}
		}

		// generate non-linear transformation table
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int angle, distance;
				final float ratio = 32.0f;
				distance = (int) Math.floor(ratio * TEX_HEIGHT / Math.sqrt((x - w / 2.0) * (x - w / 2.0) + (y - h / 2.0) * (y - h / 2.0)) % TEX_HEIGHT);
				angle = (int) Math.floor(0.5 * TEX_WIDTH * Math.atan2(y - h / 2.0, x - w / 2.0) / 3.1416);
				distanceTable[x][y] = distance;
				angleTable[x][y] = angle;
			}
		}

		animation = 0f;
		redrawCanvas();
	}

	public void animate() {
		// calculate the shift values out of the animation value
		final int shiftX = (int) Math.floor(TEX_WIDTH * 1.0 * animation);
		final int shiftY = (int) Math.floor(TEX_HEIGHT * 0.25 * animation);

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				// get the texel from the texture by using the tables, shifted with the animation values
				final int color = texture[Math.abs((distanceTable[x][y] + shiftX) % TEX_WIDTH)][Math.abs((angleTable[x][y] + shiftY) % TEX_HEIGHT)];
				imageData.setPixel(x, y, color);
			}
		}
		animation += 0.05f;
		redrawCanvas();

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
		final Image img = new Image(display, imageData);
		if (img != null) {
			gc.drawImage(img, 0, 0);
		}
		img.dispose();

	}

	public static void main(String[] args) {
		final Display display = new Display();

		final Tunnel1 app = new Tunnel1(display);

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
