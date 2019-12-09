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
package org.mihalis.demos.explosion;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Explosion {

	// Title
	private static final String SHELL_TITLE = "Explosion";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 480;
	private static final int CANVAS_HEIGHT = 360;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private int w, h;
	private ImageData imageData;

	private static final int NUMBER_OF_PARTICLES = 500;
	int fire[];
	Particle[] particles;

	public Explosion(Display display) {
		this.display = display;
	}

	public void init() {

		fire = new int[w * h];
		particles = new Particle[NUMBER_OF_PARTICLES];

		/* create a suitable shadebob palette, this is crucial for a good effect */
		/* black to blue, blue to red, red to white */
		final RGB[] colors = new RGB[256];
		for (int i = 0; i < 32; ++i) {
			/* black to blue, 32 values */
			colors[i] = new RGB(0, 0, i << 1);

			/* blue to red, 32 values */
			colors[i + 32] = new RGB(i << 3, 0, 64 - (i << 1));

			/* red to yellow, 32 values */
			colors[i + 64] = new RGB(255, i << 3, 0);

			/* yellow to white, 162 */
			colors[i + 96] = new RGB(255, 255, i << 2);
			colors[i + 128] = new RGB(255, 255, 64 + (i << 2));
			colors[i + 160] = new RGB(255, 255, 128 + (i << 2));
			colors[i + 192] = new RGB(255, 255, 192 + i);
			colors[i + 224] = new RGB(255, 255, 224 + i);
		}

		imageData = new ImageData(w, h, 8, new PaletteData(colors));

		initParticles(true);
	}

	private void initParticles(boolean create) {
		for (int i = 0; i < NUMBER_OF_PARTICLES; i++) {
			if (create) {
				particles[i] = new Particle();
			}

			particles[i].xpos = (w >> 1) - 20 + (int) (40.0 * Math.random());
			particles[i].ypos = (h >> 1) - 20 + (int) (40.0 * Math.random());
			particles[i].xdir = -10 + (int) (20.0 * Math.random());
			particles[i].ydir = -17 + (int) (19.0 * Math.random());
			particles[i].colorindex = 255;
			particles[i].dead = false;
		}
	}

	public void animate() {
		// Animation
		/* move and draw particles into fire array */
		int nbDead = 0;
		for (int i = 0; i < NUMBER_OF_PARTICLES; i++) {
			if (!particles[i].dead) {
				particles[i].xpos += particles[i].xdir;
				particles[i].ypos += particles[i].ydir;

				/* is particle dead? */
				if (particles[i].ypos >= h - 3 || particles[i].colorindex == 0 || particles[i].xpos <= 1 || particles[i].xpos >= w - 3) {
					particles[i].dead = true;
					continue;
				}

				/* gravity takes over */
				particles[i].ydir++;

				/* particle cools off */
				particles[i].colorindex--;

				/* draw particle */
				final int temp = particles[i].ypos * w + particles[i].xpos;

				fire[temp] = particles[i].colorindex;
				fire[temp - 1] = particles[i].colorindex;
				fire[temp + w] = particles[i].colorindex;
				fire[temp - w] = particles[i].colorindex;
				fire[temp + 1] = particles[i].colorindex;
			} else {
				nbDead++;
			}
		}

		/* create fire effect */
		for (int i = 1; i < h - 2; i++) {
			final int index = (i - 1) * w;

			for (int j = 1; j < w - 2; j++) {
				int buf = index + j;

				int temp = fire[buf];
				temp += fire[buf + 1];
				temp += fire[buf - 1];
				buf += w;
				temp += fire[buf - 1];
				temp += fire[buf + 1];
				buf += w;
				temp += fire[buf];
				temp += fire[buf + 1];
				temp += fire[buf - 1];

				temp >>= 3;

				if (temp > 4) {
					temp -= 4;
				} else {
					temp = 0;
				}

				fire[buf - w] = temp;
			}
		}

		/* draw fire array to screen from bottom to top */
		int image = w * h - 1;
		for (int i = h - 1; i >= 0; --i) {
			final int temp = i * w;
			for (int j = w - 1; j >= 0; --j) {
				imageData.setPixel(image % w, image / w, fire[temp + j]);
				image--;
			}
		}

		if (nbDead == NUMBER_OF_PARTICLES) {
			initParticles(false);
		}

		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
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

		canvas.addListener(SWT.Resize, e -> {
			w = canvas.getClientArea().width;
			h = canvas.getClientArea().height;
			init();
		});

		canvas.addPaintListener(e -> {
			redrawCanvas(e.gc);
		});

		return shell;
	}

	private void redrawCanvas(GC gc) {
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

		final Explosion app = new Explosion(display);

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
