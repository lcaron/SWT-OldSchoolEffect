/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jerry Huxtable - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.rayfilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BlockEffect {

	// Title
	private static final String TITLE = "BlockEffect";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 240;
	private static final int CANVAS_HEIGHT = 160;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	//
	private ImageData image;
	private int blockSize;
	private int sens;

	public BlockEffect(Display display) {
		this.display = display;
	}

	public void init() {

		// Load image
		image = new ImageData(getClass().getResourceAsStream("flower.jpg"));

		imageData = new ImageData(w, h, image.depth, image.palette);

		final int[] pixels = new int[w * h];
		image.getPixels(0, 0, Math.min(w * h, image.width * image.height), pixels, 0);
		imageData.setPixels(0, 0, Math.min(w * h, image.width * image.height), pixels, 0);

		blockSize = 1;
		sens = 1;

	}

	public void animate() {
		imageData = filter(image, blockSize);
		blockSize += sens;

		if (blockSize == 1) {
			sens = 1;
		}

		if (blockSize == w / 4) {
			sens = -1;
		}
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private ImageData filter(ImageData src, int blockSize) {
		final ImageData dst = new ImageData(w, h, src.depth, src.palette);
		final int width = src.width;
		final int height = src.height;

		final int[] pixels = new int[blockSize * blockSize];
		for (int y = 0; y < height; y += blockSize) {
			for (int x = 0; x < width; x += blockSize) {
				final int w = Math.min(blockSize, width - x);
				final int h = Math.min(blockSize, height - y);
				final int t = w * h;
				getRGB(src, x, y, w, h, pixels, 0, w);
				int r = 0, g = 0, b = 0;
				int argb;
				int i = 0;
				for (int by = 0; by < h; by++) {
					for (int bx = 0; bx < w; bx++) {
						argb = pixels[i];
						r += argb >> 16 & 0xff;
						g += argb >> 8 & 0xff;
						b += argb & 0xff;
						i++;
					}
				}
				argb = r / t << 16 | g / t << 8 | b / t;
				i = 0;
				for (int by = 0; by < h; by++) {
					for (int bx = 0; bx < w; bx++) {
						pixels[i] = pixels[i] & 0xff000000 | argb;
						i++;
					}
				}
				setRGB(dst, x, y, w, h, pixels, 0, w);
			}
		}

		return dst;
	}

	public void getRGB(ImageData image, int startX, int startY, int w, int h, int[] pixels, int offset, int scansize) {
		if (pixels == null) {
			pixels = new int[offset + h * scansize];
		}

		int yoff = offset;
		int off;
		for (int y = startY; y < startY + h; y++, yoff += scansize) {
			off = yoff;
			for (int x = startX; x < startX + w; x++) {
				pixels[off++] = image.getPixel(x, y);
			}
		}

	}

	public void setRGB(ImageData image, int startX, int startY, int w, int h, int[] pixels, int offset, int scansize) {
		int yoff = offset;
		int off;
		for (int y = startY; y < startY + h; y++, yoff += scansize) {
			off = yoff;
			for (int x = startX; x < startX + w; x++) {
				image.setPixel(x, y, pixels[off++]);
			}
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

		final BlockEffect app = new BlockEffect(display);

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
