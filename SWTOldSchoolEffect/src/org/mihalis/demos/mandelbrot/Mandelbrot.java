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
package org.mihalis.demos.mandelbrot;

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

public class Mandelbrot {

	// Title
	private static final String SHELL_TITLE = "Mandelbrot";

	// Size of the canvas
	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 480;

	private final int MAX_ITER = 570;
	private final double ZOOM = 150;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	public Mandelbrot(Display display) {
		this.display = display;
	}

	public void init() {
		imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
	}

	public void draw() {
		drawMandelbrot();
		if (!canvas.isDisposed()) {
			canvas.redraw();
		}
	}

	private void drawMandelbrot() {
		for (int y = 0; y < CANVAS_HEIGHT; y++) {
			for (int x = 0; x < CANVAS_WIDTH; x++) {
				double zx = 0;
				double zy = 0;
				final double cX = (x - CANVAS_WIDTH / 2) / ZOOM;
				final double cY = (y - CANVAS_HEIGHT / 2) / ZOOM;
				double tmp;
				int iter = MAX_ITER;
				while (zx * zx + zy * zy < 4 && iter > 0) {
					tmp = zx * zx - zy * zy + cX;
					zy = 2.0 * zx * zy + cY;
					zx = tmp;
					iter--;
				}
				imageData.setPixel(x, y, iter | iter << 8);
			}
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

		final Mandelbrot app = new Mandelbrot(display);

		final Shell shell = app.createWindow();
		shell.pack();
		shell.open();
		app.init();

		app.draw();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}
}
