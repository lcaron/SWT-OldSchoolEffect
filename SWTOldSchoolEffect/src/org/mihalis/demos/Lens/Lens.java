/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * W.P. van Paassen & Byron Ellacott - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.Lens;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Lens {

	// Title
	private static final String SHELL_TITLE = "Lens";

	private static final int CANVAS_WIDTH = 340;
	private static final int CANVAS_HEIGHT = 360;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	//
	private static final int LENS_WIDTH = 150;
	private static final int LENS_ZOOM = 40;
	private ImageData backing;
	private int[][] lens;
	private int _x = 16, _y = 16;
	private int xd = 1, yd = 1;

	public Lens(Display display) {
		this.display = display;
	}

	public void init() {

		// Load image
		backing = new ImageData(getClass().getResourceAsStream("tuxblackbg.png"));

		lens = new int[LENS_WIDTH][LENS_WIDTH];

		/* generate the lens distortion */
		final int r = LENS_WIDTH / 2;
		final int d = LENS_ZOOM;

		/*
		 * the shift in the following expression is a function of the distance
		 * of the current point from the center of the sphere. If you imagine:
		 *
		 * eye
		 *
		 * .-~~~~~~~-. sphere surface .` '. --------------- viewing plane .
		 * center of sphere
		 *
		 * For each point across the viewing plane, draw a line from the point
		 * on the sphere directly above that point to the center of the sphere.
		 * It will intersect the viewing plane somewhere closer to the center of
		 * the sphere than the original point. The shift function below is the
		 * end result of the above math, given that the height of the point on
		 * the sphere can be derived from:
		 *
		 * x^2 + y^2 + z^2 = radius^2
		 *
		 * x and y are known, z is based on the height of the viewing plane.
		 *
		 * The radius of the sphere is the distance from the center of the
		 * sphere to the edge of the viewing plane, which is a neat little
		 * triangle. If d = the distance from the center of the sphere to the
		 * center of the plane (aka, LENS_ZOOM) and r = half the width of the
		 * plane (aka, LENS_WIDTH/2) then radius^2 = d^2 + r^2.
		 *
		 * Things become simpler if we take z=0 to be at the plane's height
		 * rather than the center of the sphere, turning the z^2 in the
		 * expression above to (z+d)^2, since the center is now at (0, 0, -d).
		 *
		 * So, the resulting function looks like:
		 *
		 * x^2 + y^2 + (z+d)^2 = d^2 + r^2
		 *
		 * Expand the (z-d)^2:
		 *
		 * x^2 + y^2 + z^2 + 2dz + d^2 = d^2 + r^2
		 *
		 * Rearrange things to be a quadratic in terms of z:
		 *
		 * z^2 + 2dz + x^2 + y^2 - r^2 = 0
		 *
		 * Note that x, y, and r are constants, so apply the quadratic formula:
		 *
		 * For ax^2 + bx + c = 0,
		 *
		 * x = (-b +- sqrt(b^2 - 4ac)) / 2a
		 *
		 * We can ignore the negative result, because we want the point at the
		 * top of the sphere, not at the bottom.
		 *
		 * x = (-2d + sqrt(4d^2 - 4 * (x^2 + y^2 - r^2))) / 2
		 *
		 * Note that you can take the -4 out of both expressions in the square
		 * root to put -2 outside, which then cancels out the division:
		 *
		 * z = -d + sqrt(d^2 - (x^2 + y^2 - r^2))
		 *
		 * This now gives us the height of the point on the sphere directly
		 * above the equivalent point on the plane. Next we need to find where
		 * the line between this point and the center of the sphere at (0, 0,
		 * -d) intersects the viewing plane at (?, ?, 0). This is a matter of
		 * the ratio of line below the plane vs the total line length,
		 * multiplied by the (x,y) coordinates. This ratio can be worked out by
		 * the height of the line fragment below the plane, which is d, and the
		 * total height of the line, which is d + z, or the height above the
		 * plane of the sphere surface plus the height of the plane above the
		 * center of the sphere.
		 *
		 * ratio = d/(d + z)
		 *
		 * Subsitute in the formula for z:
		 *
		 * ratio = d/(d + -d + sqrt(d^2 - (x^2 + y^2 - r^2))
		 *
		 * Simplify to:
		 *
		 * ratio = d/sqrt(d^2 - (x^2 + y^2 - r^2))
		 *
		 * Since d and r are constant, we now have a formula we can apply for
		 * each (x,y) point within the sphere to give the (x',y') coordinates of
		 * the point we should draw to project the image on the plane to the
		 * surface of the sphere. I subtract the original (x,y) coordinates to
		 * give an offset rather than an absolute coordinate, then convert that
		 * offset to the image dimensions, and store the offset in a matrix the
		 * size of the intersecting circle. Drawing the lens is then a matter
		 * of:
		 *
		 * screen[coordinate] = image[coordinate + lens[y][x]]
		 */

		/*
		 * it is sufficient to generate 1/4 of the lens and reflect this around;
		 * a sphere is mirrored on both the x and y axes
		 */
		for (int y = 0; y < LENS_WIDTH >> 1; y++) {
			for (int x = 0; x < LENS_WIDTH >> 1; x++) {
				int ix, iy, offset;
				if (x * x + y * y < r * r) {
					final float shift = (float) (d / Math.sqrt(d * d - (x * x + y * y - r * r)));
					ix = (int) (x * shift - x);
					iy = (int) (y * shift - y);
				} else {
					ix = 0;
					iy = 0;
				}
				offset = iy * w + ix;
				lens[LENS_WIDTH / 2 - y][LENS_WIDTH / 2 - x] = -offset;
				lens[LENS_WIDTH / 2 + y][LENS_WIDTH / 2 + x] = offset;
				offset = -iy * w + ix;
				lens[LENS_WIDTH / 2 + y][LENS_WIDTH / 2 - x] = -offset;
				lens[LENS_WIDTH / 2 - y][LENS_WIDTH / 2 + x] = offset;
			}
		}

		imageData = new ImageData(w, h, backing.depth, backing.palette);
		final int[] pixels = new int[w * h];
		backing.getPixels(0, 0, w * h, pixels, 0);
		imageData.setPixels(0, 0, w * h, pixels, 0);

		redrawCanvas();
	}

	public void animate() {
		// Animation
		/* apply the lens */
		applyLens(_x, _y);

		/* shift the coordinates around */
		_x += xd;
		_y += yd;
		if (_x > w - LENS_WIDTH - 15 || _x < 15) {
			xd = -xd;
		}
		if (_y > h - LENS_WIDTH - 15 || _y < 15) {
			yd = -yd;
		}

		redrawCanvas();
	}

	private void applyLens(int ox, int oy) {
		int x, y, temp, pos;

		for (y = 0; y < LENS_WIDTH; y++) {
			temp = (y + oy) * w + ox;
			for (x = 0; x < LENS_WIDTH; x++) {
				pos = temp + x;
				final int backPos = pos + lens[y][x];
				imageData.setPixel(pos % w, pos / w, backing.getPixel(backPos % w, backPos / w));
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

		final Lens app = new Lens(display);

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
