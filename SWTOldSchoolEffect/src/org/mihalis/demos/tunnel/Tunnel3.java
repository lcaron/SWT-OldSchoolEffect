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

public class Tunnel3 {

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
	private static final int TIMER_INTERVAL = 1;

	public Tunnel3(Display display) {
		this.display = display;
	}

	public void init() {
		imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
		texture = new int[TEX_WIDTH][TEX_HEIGHT];
		distanceTable = new int[w * 2][h * 2];
		angleTable = new int[w * 2][h * 2];

		final Image img = new Image(display, getClass().getResourceAsStream("tunnelstonetex.png"));
		final ImageData temp = img.getImageData();

		// generate texture
		for (int x = 0; x < TEX_WIDTH; x++) {
			for (int y = 0; y < TEX_HEIGHT; y++) {
				texture[x][y] = temp.getPixel(x, y);
			}
		}

		// generate non-linear transformation table
		for (int x = 0; x < w * 2; x++) {
			for (int y = 0; y < h * 2; y++) {
				int angle, distance;
				final float ratio = 32.0f;
				distance = (int) (ratio * TEX_HEIGHT / Math.sqrt((x - w) * (x - w) + (y - h) * (y - h))) % TEX_HEIGHT;
				angle = (int) (0.5 * TEX_WIDTH * Math.atan2(y - h, (x - w) / 3.1416));
				distanceTable[x][y] = distance;
				angleTable[x][y] = angle;
			}
		}

		animation = 0f;
		redrawCanvas();
	}

	public void animate() {
		// calculate the shift values out of the animation value
		final int shiftX = (int) (TEX_WIDTH * 1.0 * animation);
		final int shiftY = (int) (TEX_HEIGHT * 0.25 * animation);

		// calculate the look values out of the animation value
		// by using sine functions, it'll alternate between looking left/right and up/down
		// make sure that x + shiftLookX never goes outside the dimensions of the table, same for y
		final int shiftLookX = w / 2 + (int) (w / 2 * Math.sin(animation));
		final int shiftLookY = h / 2 + (int) (h / 2 * Math.sin(animation * 2.0));

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				// get the texel from the texture by using the tables, shifted with the animation values
				final int color = texture[Math.abs((distanceTable[x + shiftLookX][y + shiftLookY] + shiftX) % TEX_WIDTH)][Math.abs((angleTable[x + shiftLookX][y + shiftLookY] + shiftY) % TEX_HEIGHT)];
				imageData.setPixel(x, y, color);
			}
		}
		animation += 0.02f;
		redrawCanvas();

	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("Tunnel");
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

		final Tunnel3 app = new Tunnel3(display);

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
