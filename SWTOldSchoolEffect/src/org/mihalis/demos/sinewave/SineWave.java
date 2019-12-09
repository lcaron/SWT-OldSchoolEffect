/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * W.P. van Paassen - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.sinewave;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SineWave {

	// Title
	private static final String SHELL_TITLE = "SineWave";

	private static final int CANVAS_WIDTH = 340;
	private static final int CANVAS_HEIGHT = 360;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	//
	private static final int MAX_EFFECTS = 12;
	private int sin_index;
	private ImageData image;
	private SinEffect sine_effects[];
	int current_effect;

	public SineWave(Display display) {
		this.display = display;
	}

	public void init() {
		sin_index = 0;

		// Load image
		image = new ImageData(getClass().getResourceAsStream("tuxblackbg.png"));

		imageData = new ImageData(w, h, image.depth, image.palette);
		final int[] pixels = new int[w * h];
		image.getPixels(0, 0, w * h, pixels, 0);
		imageData.setPixels(0, 0, w * h, pixels, 0);

		sine_effects = new SinEffect[SineWave.MAX_EFFECTS];
		current_effect = 0;

		for (int i = 0; i < MAX_EFFECTS; i++) {
			sine_effects[i] = new SinEffect();
		}

		for (int i = 0; i < 512; ++i) {
			final float rad = (float) (i * 0.0174532 * 0.703125);

			sine_effects[0].sine_table[i] = (short) (Math.sin(rad) * 8.0);
			sine_effects[1].sine_table[i] = (short) (Math.sin(rad) * 16.0);
			sine_effects[2].sine_table[i] = (short) (Math.sin(rad) * 20.0);
			sine_effects[3].sine_table[i] = (short) (Math.sin(rad) * 32.0);
			sine_effects[4].sine_table[i] = (short) (Math.sin(rad) * 64.0);
			sine_effects[5].sine_table[i] = (short) (Math.sin(rad) * 128.0);
			sine_effects[6].sine_table[i] = (short) (Math.sin(rad) * 256.0);
			sine_effects[7].sine_table[i] = (short) (Math.sin(rad) * 128.0);
			sine_effects[8].sine_table[i] = (short) (Math.sin(rad) * 64.0);
			sine_effects[9].sine_table[i] = (short) (Math.sin(rad) * 44.0);
			sine_effects[10].sine_table[i] = (short) (Math.sin(rad) * 32.0);
			sine_effects[11].sine_table[i] = (short) (Math.sin(rad) * 8.0);
		}

		sine_effects[0].index_add = 2;
		sine_effects[0].effect = false;

		sine_effects[1].index_add = 4;
		sine_effects[1].effect = false;

		sine_effects[2].index_add = 3;
		sine_effects[2].effect = true;

		sine_effects[3].index_add = 5;
		sine_effects[3].effect = false;

		sine_effects[4].index_add = 2;
		sine_effects[4].effect = false;

		sine_effects[5].index_add = 2;
		sine_effects[5].effect = true;

		sine_effects[6].index_add = 4;
		sine_effects[6].effect = false;

		sine_effects[7].index_add = 1;
		sine_effects[7].effect = false;

		sine_effects[8].index_add = 2;
		sine_effects[8].effect = true;

		sine_effects[9].index_add = 8;
		sine_effects[9].effect = false;

		sine_effects[10].index_add = 3;
		sine_effects[10].effect = true;

		sine_effects[11].index_add = 2;
		sine_effects[11].effect = true;

		redrawCanvas();
	}

	public void animate() {
		// Animation
		int sin_backup = sin_index;
		final Rectangle d = new Rectangle(0, 0, w, 1);
		final Rectangle r = new Rectangle(sine_effects[current_effect].sine_table[sin_backup], 0, w, 1);

		if (sine_effects[current_effect].effect) {
			for (int i = 0; i < image.height; ++i) {
				copy_image(image, r, imageData, d);
				r.y = i;
				d.y = i;
				d.x = 0;
				sin_backup += sine_effects[current_effect].index_add;
				sin_backup &= 511;

				if (i % 2 == 1) {
					r.x = sine_effects[current_effect].sine_table[sin_backup];
				} else {
					r.x = -sine_effects[current_effect].sine_table[sin_backup];
				}
			}
		} else {
			for (int i = 0; i < image.height; ++i) {
				copy_image(image, r, imageData, d);
				r.y = i;
				d.y = i;
				d.x = 0;
				sin_backup += sine_effects[current_effect].index_add;
				sin_backup &= 511;
				r.x = sine_effects[current_effect].sine_table[sin_backup];
			}
		}

		sin_index += 6;
		if (sin_index > 511) {
			sin_index = 0;
			current_effect++;
			current_effect %= MAX_EFFECTS;

		}
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private void copy_image(ImageData src, Rectangle rSrc, ImageData dest, Rectangle rDest) {
		if (rSrc.x >= 0) {
			final int[] pixels = new int[rSrc.width * rSrc.height];
			src.getPixels(rSrc.x, rSrc.y, rSrc.width * rSrc.height, pixels, 0);
			dest.setPixels(rDest.x, rDest.y, rSrc.width * rSrc.height, pixels, 0);
		} else {
			final int[] pixels = new int[rSrc.width * rSrc.height];

			src.getPixels(0, rSrc.y, rSrc.width * rSrc.height + rSrc.x, pixels, 0);
			dest.setPixels(rDest.x - rSrc.x, rDest.y, rSrc.width * rSrc.height, pixels, 0);

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

		final SineWave app = new SineWave(display);

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
