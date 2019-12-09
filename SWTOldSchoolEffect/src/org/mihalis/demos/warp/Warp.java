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
package org.mihalis.demos.warp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Warp {

	// Title
	private static final String SHELL_TITLE = "Warp Effect";

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

	private final Point[][] distorsionTable = new Point[CANVAS_HEIGHT / 2][CANVAS_WIDTH / 2];
	private static final int TEXTUREWIDTH = 256;
	private static final int TEXTUREHEIGHT = 256;
	private int[] texture;
	private float alpha = 0, beta = 0, dz = 0, dw = 0;

	public Warp(Display display) {
		this.display = display;
	}

	public void init() {
		initDistortionTable();
		final ImageData image = new ImageData(getClass().getResourceAsStream("texture.png"));
		imageData = new ImageData(w, h, image.depth, image.palette);

		int index = 0;
		texture = new int[TEXTUREWIDTH * TEXTUREHEIGHT];
		for (int x = 0; x < TEXTUREWIDTH; x++) {
			for (int y = 0; y < TEXTUREHEIGHT; y++) {
				texture[index++] = image.getPixel(x, y);
			}
		}
	}

	private void initDistortionTable() {
		for (int i = 0; i < CANVAS_HEIGHT / 2; i++) {
			for (int j = 0; j < CANVAS_WIDTH / 2; j++) {
				double f = Math.pow(j * 1.2 / CANVAS_WIDTH, 2);
				double d = Math.log(1 + i / (CANVAS_HEIGHT / 3.0)) / (3 * f + 1) * CANVAS_HEIGHT / 2;
				final short w = (short) d;

				f = Math.pow(i * 1.5 / CANVAS_WIDTH, 2);
				d = Math.log(1 + j / (CANVAS_WIDTH / 3.0)) / (3 * f + 1) * CANVAS_WIDTH / 2;
				final short z = (short) d;

				distorsionTable[i][j] = new Point(w, z);
			}
		}
	}

	public void animate() {
		doWarp();
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private void doWarp() {
		alpha += 0.02f;
		beta += 0.044f;
		dz += Math.sin(alpha + beta) * 2 + Math.cos(beta) + 0.4;
		dw += Math.cos(beta - alpha) * 3 + Math.sin(alpha) + 0.2;
		final int decz = (int) dz;
		final int decw = (int) dw;

		for (int j = 0; j < CANVAS_HEIGHT / 2; j++) {
			int p1 = CANVAS_WIDTH / 2 + CANVAS_WIDTH * (CANVAS_HEIGHT / 2 - j);
			int p2 = p1;
			int p3 = CANVAS_WIDTH / 2 + CANVAS_WIDTH * (CANVAS_HEIGHT / 2 + j);
			int p4 = p3;

			for (int i = 0; i < CANVAS_WIDTH / 2; i++) {
				final int ddx = distorsionTable[j][i].x;
				final int ddy = distorsionTable[j][i].y;

				drawPixel(p1, -ddx + decz & 255, -ddy + decw & 255);
				p1--;
				drawPixel(p2, -ddx + decz & 255, ddy + decw & 255);
				p2++;
				drawPixel(p3, ddx + decz & 255, -ddy + decw & 255);
				p3--;
				drawPixel(p4, ddx + decz & 255, ddy + decw & 255);
				p4++;
			}
		}
	}

	private void drawPixel(int index, int x, int y) {
		final int colorIndex = getTexturePixel(x, y);
		final int posX = index % CANVAS_WIDTH;
		final int posY = index / CANVAS_WIDTH;
		imageData.setPixel(posX, posY, colorIndex);
	}

	private int getTexturePixel(int x, int y) {
		return texture[(y << 8) + x];
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

		final Warp app = new Warp(display);

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
