/*******************************************************************************
 * Copyright (c) 2019 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Copyright (C) Calogiuri Enzo Antonio 2011 <Insolit Dust - http://insolitdust.sourceforge.net/code.html> - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.wormhole;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Wormhole {

	// Title
	private static final String SHELL_TITLE = "Wormhole";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 480;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 30;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	private byte[] wormImg;
	private float[] spokeCalc;
	private float[] spokeCosCalc;
	private float[] spokeSinCalc;
	private int[] wormTexture;

	//
	private static final int DIRECTIONX = -2; // -5 to 5
	private static final int DIRECTIONY = -1; // -5 to 5
	private static final int SPOKES = 2400;
	private static final int MAXX = 640;
	private static final int MAXY = 480;
	private static final int XCENTER = MAXX / 2;
	private static final int YCENTER = MAXY / 2 - MAXY / 4;
	private static final int DIVS = SPOKES;
	private static final int TEXTUREWIDTH = 15;
	private static final int TEXTUREHEIGHT = 15;

	public Wormhole(Display display) {
		this.display = display;
	}

	public void init() {
		initTables();
		final ImageData image = new ImageData(getClass().getResourceAsStream("texture.png"));
		imageData = new ImageData(w, h, image.depth, image.palette);

		int index = 0;
		wormTexture = new int[TEXTUREWIDTH * TEXTUREHEIGHT];
		for (int x = 0; x < TEXTUREWIDTH; x++) {
			for (int y = 0; y < TEXTUREHEIGHT; y++) {
				wormTexture[index++] = image.getPixel(x, y);
			}
		}

		redrawCanvas();
	}

	private void initTables() {
		wormImg = new byte[MAXX * MAXY];
		spokeCalc = new float[SPOKES];
		spokeCosCalc = new float[SPOKES];
		spokeSinCalc = new float[SPOKES];

		for (int i = 0; i < SPOKES; i++) {
			spokeCalc[i] = (float) (2f * Math.PI * i / SPOKES);
			spokeCosCalc[i] = (float) Math.cos(spokeCalc[i]);
			spokeSinCalc[i] = (float) Math.sin(spokeCalc[i]);
		}

		for (int j = 1; j < DIVS + 1; j++) {
			final float z = (float) (-1.0f + Math.log(2.0f * j / DIVS));

			final float divCalcX = MAXX * j / DIVS;
			final float divCalcY = MAXY * j / DIVS;

			for (int i = 0; i < SPOKES; i++) {
				float x = divCalcX * spokeCosCalc[i];
				float y = divCalcY * spokeSinCalc[i];

				y -= 25f * z;

				x += XCENTER;
				y += YCENTER;

				if (x >= 0 && x < MAXX && y >= 0 && y < MAXY) {
					wormImg[(int) x
							+ (int) y * MAXX] = (byte) (i / 8 % TEXTUREWIDTH + TEXTUREWIDTH * (j / 7 % TEXTUREWIDTH));
				}
			}
		}
	}

	public void animate() {
		doWormhole();
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private void doWormhole() {

		for (int i = 0; i < MAXX * MAXY; i++) {
			final int colorIndex = wormTexture[(wormImg[i] + 256) % 256];
			imageData.setPixel(i % CANVAS_WIDTH, i / CANVAS_WIDTH, colorIndex);
		}

		shiftDown();
		shiftUp();
		shiftRight();
		shiftLeft();
	}

	private void shiftDown() {
		final int[] reg = new int[TEXTUREHEIGHT];

		for (int i = 0; i < DIRECTIONY; i++) {
			for (int k = 0; k < TEXTUREHEIGHT; k++) {
				reg[k] = wormTexture[k + 210];
			}

			for (int k = 209; k >= 0; k--) {
				wormTexture[k + 15] = wormTexture[k];
			}

			for (int k = 0; k < 15; k++) {
				wormTexture[k] = reg[k];
			}
		}
	}

	private void shiftUp() {
		final int[] reg = new int[TEXTUREHEIGHT];

		for (int i = DIRECTIONY; i < 0; i++) {
			for (int k = 0; k < TEXTUREHEIGHT; k++) {
				reg[k] = wormTexture[k];
			}

			for (int k = 15; k < 15 * 15; k++) {
				wormTexture[k - 15] = wormTexture[k];
			}

			for (int k = 0; k < 15; k++) {
				wormTexture[k + 210] = reg[k];
			}
		}
	}

	private void shiftRight() {
		final int[] reg = new int[TEXTUREHEIGHT];

		for (int j = 0; j < DIRECTIONX; j++) {
			for (int k = 0; k < 15; k++) {
				reg[k] = wormTexture[15 * k + 14];

				for (int i = 14; i > 0; i--) {
					wormTexture[15 * k + i] = wormTexture[15 * k + i - 1];
				}

				wormTexture[15 * k] = reg[k];
			}
		}
	}

	private void shiftLeft() {
		final int[] reg = new int[TEXTUREHEIGHT];

		for (int j = DIRECTIONX; j < 0; j++) {
			for (int k = 0; k < 15; k++) {
				reg[k] = wormTexture[15 * k];

				for (int i = 0; i < 14; i++) {
					wormTexture[15 * k + i] = wormTexture[15 * k + i + 1];
				}

				wormTexture[15 * k + 14] = reg[k];
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

		final Wormhole app = new Wormhole(display);

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
