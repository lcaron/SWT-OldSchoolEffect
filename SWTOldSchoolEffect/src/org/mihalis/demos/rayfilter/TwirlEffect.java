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

public class TwirlEffect {

	// Title
	private static final String TITLE = "TwirlEffect";

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
	private float angle;
	private float centreX;
	private float centreY;
	private float radius;

	private float radius2;
	private float icentreX;
	private float icentreY;

	private float sens;
	private static final float STEP = 0.1f;

	public TwirlEffect(Display display) {
		this.display = display;
	}

	public void init() {

		// Load image
		image = new ImageData(getClass().getResourceAsStream("flower.jpg"));

		imageData = new ImageData(w, h, image.depth, image.palette);

		// Copy image
		final int[] pixels = new int[w * h];
		image.getPixels(0, 0, Math.min(w * h, image.width * image.height), pixels, 0);
		imageData.setPixels(0, 0, Math.min(w * h, image.width * image.height), pixels, 0);

		//
		angle = 0;
		centreX = 0.5f;
		centreY = 0.5f;
		radius = 100;

		icentreX = image.width * centreX;
		icentreY = image.height * centreY;
		if (radius == 0) {
			radius = Math.min(icentreX, icentreY);
		}
		radius2 = radius * radius;

		radius2 = radius * radius;

		sens = STEP;

		redrawCanvas();
	}

	public void animate() {
		imageData = filter(image, new ImageData(w, h, image.depth, image.palette));

		angle += sens;

		if (angle < -15.7) {
			sens = STEP;
		}

		if (angle > 15.7) { // 5 * PI
			sens = -1 * STEP;
		}
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private ImageData filter(ImageData src, ImageData dst) {
		final int width = src.width;
		final int height = src.height;

		// Copy pixels in inPixels
		final int[] inPixels = new int[width * height];
		src.getPixels(0, 0, width * height, inPixels, 0);

		final int srcWidth = width;
		final int srcHeight = height;
		final int srcWidth1 = width - 1;
		final int srcHeight1 = height - 1;
		final int outWidth = width;
		final int outHeight = height;
		int outX, outY;
		final int[] outPixels = new int[outWidth];

		outX = 0;
		outY = 0;
		final float[] out = new float[2];

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				transformInverse(outX + x, outY + y, out);
				final int srcX = (int) Math.floor(out[0]);
				final int srcY = (int) Math.floor(out[1]);
				final float xWeight = out[0] - srcX;
				final float yWeight = out[1] - srcY;
				int nw, ne, sw, se;

				if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
					// Easy case, all corners are in the image
					final int i = srcWidth * srcY + srcX;
					nw = inPixels[i];
					ne = inPixels[i + 1];
					sw = inPixels[i + srcWidth];
					se = inPixels[i + srcWidth + 1];
				} else {
					// Some of the corners are off the image
					nw = getPixel(inPixels, srcX, srcY, srcWidth, srcHeight);
					ne = getPixel(inPixels, srcX + 1, srcY, srcWidth, srcHeight);
					sw = getPixel(inPixels, srcX, srcY + 1, srcWidth, srcHeight);
					se = getPixel(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight);
				}
				outPixels[x] = bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
			}
			setRGB(dst, 0, y, width, 1, outPixels, 0, width);
		}
		return dst;
	}

	final private int getPixel(int[] pixels, int x, int y, int width, int height) {
		return pixels[clamp(y, 0, height - 1) * width + clamp(x, 0, width - 1)];
	}

	/**
	 * Bilinear interpolation of ARGB values.
	 *
	 * @param x   the X interpolation parameter 0..1
	 * @param y   the y interpolation parameter 0..1
	 * @param rgb array of four ARGB values in the order NW, NE, SW, SE
	 * @return the interpolated value
	 */
	private int bilinearInterpolate(float x, float y, int nw, int ne, int sw, int se) {
		float m0, m1;
		final int a0 = nw >> 24 & 0xff;
		final int r0 = nw >> 16 & 0xff;
		final int g0 = nw >> 8 & 0xff;
		final int b0 = nw & 0xff;
		final int a1 = ne >> 24 & 0xff;
		final int r1 = ne >> 16 & 0xff;
		final int g1 = ne >> 8 & 0xff;
		final int b1 = ne & 0xff;
		final int a2 = sw >> 24 & 0xff;
		final int r2 = sw >> 16 & 0xff;
		final int g2 = sw >> 8 & 0xff;
		final int b2 = sw & 0xff;
		final int a3 = se >> 24 & 0xff;
		final int r3 = se >> 16 & 0xff;
		final int g3 = se >> 8 & 0xff;
		final int b3 = se & 0xff;

		final float cx = 1.0f - x;
		final float cy = 1.0f - y;

		m0 = cx * a0 + x * a1;
		m1 = cx * a2 + x * a3;
		final int a = (int) (cy * m0 + y * m1);

		m0 = cx * r0 + x * r1;
		m1 = cx * r2 + x * r3;
		final int r = (int) (cy * m0 + y * m1);

		m0 = cx * g0 + x * g1;
		m1 = cx * g2 + x * g3;
		final int g = (int) (cy * m0 + y * m1);

		m0 = cx * b0 + x * b1;
		m1 = cx * b2 + x * b3;
		final int b = (int) (cy * m0 + y * m1);

		return a << 24 | r << 16 | g << 8 | b;
	}

	/**
	 * Clamp a value to an interval.
	 *
	 * @param a the lower clamp threshold
	 * @param b the upper clamp threshold
	 * @param x the input parameter
	 * @return the clamped value
	 */
	private int clamp(int x, int a, int b) {
		return x < a ? a : x > b ? b : x;
	}

	private void transformInverse(int x, int y, float[] out) {
		final float dx = x - icentreX;
		final float dy = y - icentreY;
		float distance = dx * dx + dy * dy;
		if (distance > radius2) {
			out[0] = x;
			out[1] = y;
		} else {
			distance = (float) Math.sqrt(distance);
			final float a = (float) Math.atan2(dy, dx) + angle * (radius - distance) / radius;
			out[0] = icentreX + distance * (float) Math.cos(a);
			out[1] = icentreY + distance * (float) Math.sin(a);
		}
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

		final TwirlEffect app = new TwirlEffect(display);

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
