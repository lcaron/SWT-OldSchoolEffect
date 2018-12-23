/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Griffiths - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.lake;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Original by
 */
public class LakeEffect {

	// Title
	private static final String SHELL_TITLE = "LakeEffect";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 306;
	private static final int CANVAS_HEIGHT = 300;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 100;

	private Display display;
	private Canvas canvas;
	private GC gc;

	//
	private Image image;
	private int imageWidth, imageHeight;
	private Image waveImage;
	private int currImage;

	public LakeEffect(Display display) {
		this.display = display;
	}

	public void init() {

		// Load image
		image = new Image(display, getClass().getResourceAsStream("ash.jpg"));
		imageWidth = image.getBounds().width;
		imageHeight = image.getBounds().height;

		// Create animation
		createAnimation();

		redrawCanvas();
	}

	public void animate() {
		if (++currImage == 12) {
			currImage = 0;
		}

		redrawCanvas();
	}

	private void createAnimation() {
		final Image localImage = new Image(display, imageWidth, imageHeight + 1);
		final GC localGraphics = new GC(localImage);
		localGraphics.drawImage(image, 0, 1);

		for (int i = 0; i < imageHeight >> 1; ++i) {
			copyArea(localGraphics, 0, i, imageWidth, 1, 0, imageHeight - i);
			copyArea(localGraphics, 0, imageHeight - 1 - i, imageWidth, 1, 0, -imageHeight + 1 + (i << 1));
			copyArea(localGraphics, 0, imageHeight, imageWidth, 1, 0, -1 - i);
		}

		waveImage = new Image(display, 13 * imageWidth, imageHeight);
		final GC waveGraphics = new GC(waveImage);
		waveGraphics.drawImage(localImage, 12 * imageWidth, 0);
		int j = 0;
		do {
			makeWaves(waveGraphics, j);
		} while (++j < 12);
		waveGraphics.dispose();

		localGraphics.drawImage(image, 0, 1);
		localGraphics.dispose();

		image = localImage;
	}

	private void makeWaves(GC paramGraphics, int paramInt) {
		final double d = 6.283185307179586D * paramInt / 12.0D;
		final int i = (12 - paramInt) * imageWidth;
		for (int j = 0; j < imageHeight; ++j) {
			final int k = (int) (imageHeight / 14 * (j + 28.0D) * Math.sin(imageHeight / 14 * (imageHeight - j) / (j + 1) + d) / imageHeight);
			if (j < -k) {
				copyArea(paramGraphics, 12 * imageWidth, j, imageWidth, 1, -i, 0);
			} else {
				copyArea(paramGraphics, 12 * imageWidth, j + k, imageWidth, 1, -i, -k);
			}
		}

	}

	/**
	 * @param x the <i>x</i> coordinate of the source rectangle.
	 * @param y the <i>y</i> coordinate of the source rectangle.
	 * @param width the width of the source rectangle.
	 * @param height the height of the source rectangle.
	 * @param dx the horizontal distance to copy the pixels.
	 * @param dy the vertical distance to copy the pixels.
	 */
	private void copyArea(GC gc, int x, int y, int width, int height, int dx, int dy) {
		gc.copyArea(x, y, width, height, x + dx, y + dy);
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
			redrawCanvas();
		});

		return shell;
	}

	private void redrawCanvas() {
		if (waveImage != null) {
			gc.drawImage(waveImage, -currImage * imageWidth, imageHeight);
			gc.drawImage(waveImage, (12 - currImage) * imageWidth, imageHeight);
		}
		gc.drawImage(image, 0, -1);
	}

	public static void main(String[] args) {
		final Display display = new Display();

		final LakeEffect app = new LakeEffect(display);

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
