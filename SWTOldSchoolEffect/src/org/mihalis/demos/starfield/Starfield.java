/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Laurent CARON (laurent.caron at gmail dot com) - Original Version
 * Based on code by W.P. van Paassen - peter@paassen.tmfweb.nl
 *******************************************************************************/
package org.mihalis.demos.starfield;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Starfield {

	// Title
	private static final String SHELL_TITLE = "Starfield";

	private static final int TIMER_INTERVAL = 10;

	// Width, Height
	private static final int CANVAS_WIDTH = 480;
	private static final int CANVAS_HEIGHT = 360;

	private static final int NUMBER_OF_STARS = 1020;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private Image image;

	private Star[] stars;
	private int centerX, centerY;

	public Starfield(Display display) {
		this.display = display;
	}

	public void init() {
		image = new Image(display, w, h);

		// Init stars
		stars = new Star[NUMBER_OF_STARS];
		for (int i = 0; i < NUMBER_OF_STARS; i++) {
			stars[i] = initStar(i + 1);

		}
		centerX = w >> 1;
		centerY = h >> 1;
		redrawCanvas();
	}

	private Star initStar(int i) {
		final Star star = new Star();
		star.xpos = (float) (-10.0 + 20.0 * Math.random());
		star.ypos = (float) (-10.0 + 20.0 * Math.random());

		star.xpos *= 3072.0; /* change viewpoint */
		star.ypos *= 3072.0;

		star.zpos = i;
		star.speed = 2 + (int) (2.0 * Math.random());

		star.color = i >> 2; /* the closer to the viewer the brighter */

		return star;
	}

	public void animate() {
		// Animation
		/* move and draw stars */
		final GC gc = new GC(image);
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		gc.fillRectangle(0, 0, w, h);

		for (int i = 0; i < NUMBER_OF_STARS; i++) {
			stars[i].zpos -= stars[i].speed;

			if (stars[i].zpos <= 0) {
				stars[i] = initStar(i + 1);
			}

			/* compute 3D position */
			final int tempx = (int) (stars[i].xpos / stars[i].zpos + centerX);
			final int tempy = (int) (stars[i].ypos / stars[i].zpos + centerY);

			if (tempx < 0 || tempx > w - 1 || tempy < 0 || tempy > h - 1) /* check if a star leaves the screen */
			{
				stars[i] = initStar(i + 1);
				continue;
			}

			final Color color = new Color(Display.getCurrent(), stars[i].color, stars[i].color, stars[i].color);
			gc.setForeground(color);
			gc.drawPoint(tempx, tempy);
			color.dispose();
		}
		gc.dispose();
		if (!canvas.isDisposed()) {
			canvas.redraw();
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
		if (image != null) {
			gc.drawImage(image, 0, 0);
		}

	}

	public static void main(String[] args) {
		final Display display = new Display();

		final Starfield app = new Starfield(display);

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
