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
package org.mihalis.demos.twister;

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

public class Twister {

	// Title
	private static final String SHELL_TITLE = "Twister Effect";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 480;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 100;

	private Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	private static final int SINTABLESIZE = 256;

	// Precalculated deformation tables
	private int guim1[] = new int[SINTABLESIZE];
	private int guim2[] = new int[SINTABLESIZE];
	private int guim3[] = new int[SINTABLESIZE];

	// Precalculated torsion table
	private float pas[] = new float[SINTABLESIZE];
	private int[] palette;
	private int roll = 0;

	public Twister(Display display) {
		this.display = display;
	}

	public void init() {
		createColors();
		initSinTables();
		redrawCanvas();
	}

	private void createColors() {
		palette = new int[256];
		for (int i = 0; i < 256; i++) {
			palette[i] = createColor(i, i / 2, i / 4);
		}
	}

	private int createColor(int r, int g, int b) {
		return r << 16 | g << 8 | b;
	}

	private void initSinTables() {
		for (int i = 0; i < SINTABLESIZE; i++) {
			int min = CANVAS_WIDTH;
			int num_min = 0;
			final int tmp[] = new int[4];

			for (int k = 0; k < 4; k++) {
				tmp[k] = CANVAS_WIDTH / 2 - (int) ((CANVAS_WIDTH / 2 - 20) * Math.cos(k * Math.PI / 2 + i * (3 * Math.PI / SINTABLESIZE)));
				if (tmp[k] < min) {
					min = tmp[k];
					num_min = k;
				}
			}

			guim1[i] = tmp[num_min];
			guim2[i] = tmp[num_min + 1 & 3];
			guim3[i] = tmp[num_min + 2 & 3];

			pas[i] = (float) (1.2 * Math.sin(i * 14f * Math.PI / SINTABLESIZE) * Math.cos(i * 2f * Math.PI / SINTABLESIZE));
		}
	}

	public void animate() {
		imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
		doTwister();
		redrawCanvas();
	}

	private void doTwister() {
		roll = (roll + 1) % (SINTABLESIZE - 1);

		for (int j = 0; j < CANVAS_HEIGHT; j++) {
			int wouaf = (int) (roll + j * pas[roll]);
			wouaf &= SINTABLESIZE - 1;

			final int tmp1 = guim1[wouaf];
			final int tmp2 = guim2[wouaf];
			final int tmp3 = guim3[wouaf];

			zoom(tmp1, tmp2 - tmp1, j);
			zoom(tmp2, tmp3 - tmp2, j);
		}
	}

	private void zoom(int begin, int size, int height) {
		int screenIndex = begin + height * CANVAS_WIDTH;
		final int j = height & 63;
		final float rap = 64.0f / size;

		for (float k = 0f; k < 64f; k += rap) {
			final int i = (int) k;
			int color = (i ^ j) << 2;

			if ((color & 64) == 0) {
				color ^= 64;
			}
			drawPixel(screenIndex++, color);
		}
	}

	private void drawPixel(int screenIndex, int colorIndex) {
		final int posX = screenIndex % CANVAS_WIDTH;
		final int posY = screenIndex / CANVAS_WIDTH;
		imageData.setPixel(posX, posY, palette[colorIndex]);
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

		final Twister app = new Twister(display);

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
