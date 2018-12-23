/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ? - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.copper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Coppers {

	// Title
	private static final String SHELL_TITLE = "Coppers";

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	// Width, Height
	private static final int CANVAS_WIDTH = 480;
	private static final int CANVAS_HEIGHT = 360;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private Image image;

	//
	private int[] aSin;
	private int red, red3, red5, red7;
	private int white, white3, white5, white7;
	private int blue, blue3, blue5, blue7;
	private Rectangle drect;

	private RGB[] colors;

	public Coppers(Display display) {
		this.display = display;
	}

	public void init() {

		final int centery = h >> 1;

		/* create sin lookup table */
		aSin = new int[360];
		for (int i = 0; i < 360; i++) {
			final float rad = i * 0.0174532f;
			aSin[i] = (int) (centery + Math.sin(rad) * 100.0);
		}

		// Init palette
		colors = new RGB[46];

		for (int i = 0; i < 46; i++) {
			colors[i] = new RGB(0, 0, 0);
		}

		/* red copper bar */
		colors[1].red = 0x22;
		colors[2].red = 0x44;
		colors[3].red = 0x66;
		colors[4].red = 0x88;
		colors[5].red = 0xaa;
		colors[6].red = 0xcc;
		colors[7].red = 0xee;
		colors[8].red = 0xff;
		colors[9].red = 0xee;
		colors[10].red = 0xcc;
		colors[11].red = 0xaa;
		colors[12].red = 0x88;
		colors[13].red = 0x66;
		colors[14].red = 0x44;
		colors[15].red = 0x22;

		/* white copper bar */

		colors[16].red = 0x22;
		colors[16].green = colors[16].red;
		colors[16].blue = colors[16].green;

		colors[17].red = 0x44;
		colors[17].green = colors[17].red;
		colors[17].blue = colors[17].green;
		colors[18].red = 0x66;
		colors[18].green = colors[18].red;
		colors[18].blue = colors[18].green;
		colors[19].red = 0x88;
		colors[19].green = colors[19].red;
		colors[19].blue = colors[19].green;
		colors[20].red = 0xaa;
		colors[20].green = colors[20].red;
		colors[20].blue = colors[20].green;
		colors[21].red = 0xcc;
		colors[21].green = colors[21].red;
		colors[21].blue = colors[21].green;
		colors[22].red = 0xee;
		colors[22].green = colors[22].red;
		colors[22].blue = colors[22].green;
		colors[23].red = 0xff;
		colors[23].green = colors[23].red;
		colors[23].blue = colors[23].green;
		colors[24].red = 0xee;
		colors[24].green = colors[24].red;
		colors[24].blue = colors[24].green;
		colors[25].red = 0xcc;
		colors[25].green = colors[25].red;
		colors[25].blue = colors[25].green;
		colors[26].red = 0xaa;
		colors[26].green = colors[26].red;
		colors[26].blue = colors[26].green;
		colors[27].red = 0x88;
		colors[27].green = colors[27].red;
		colors[27].blue = colors[27].green;
		colors[28].red = 0x66;
		colors[28].green = colors[28].red;
		colors[28].blue = colors[28].green;
		colors[29].red = 0x44;
		colors[29].green = colors[29].red;
		colors[29].blue = colors[29].green;
		colors[30].red = 0x22;
		colors[30].green = colors[30].red;
		colors[30].blue = colors[30].green;

		/* blue copper bar */
		colors[31].blue = 0x22;
		colors[32].blue = 0x44;
		colors[33].blue = 0x66;
		colors[34].blue = 0x88;
		colors[35].blue = 0xaa;
		colors[36].blue = 0xcc;
		colors[37].blue = 0xee;
		colors[38].blue = 0xff;
		colors[39].blue = 0xee;
		colors[40].blue = 0xcc;
		colors[41].blue = 0xaa;
		colors[42].blue = 0x88;
		colors[43].blue = 0x66;
		colors[44].blue = 0x44;
		colors[45].blue = 0x22;

		red = 96;
		red3 = 88;
		red5 = 80;
		red7 = 72;
		white = 64;
		white3 = 56;
		white5 = 48;
		white7 = 40;

		blue = 32;
		blue3 = 24;
		blue5 = 16;
		blue7 = 8;

		drect = new Rectangle(0, 0, w, 1);

		// Init image
		image = new Image(display, w, h);

		redrawCanvas();
	}

	public void animate() {
		// Clear screen
		final GC GCTemp = new GC(image);
		GCTemp.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		GCTemp.fillRectangle(0, 0, w, h);

		/* draw copperbars back to front */

		drect.y = aSin[blue7];
		blue7 += 2;
		blue7 %= 360;

		drawCopper(GCTemp, 31);

		drect.y = aSin[blue5];
		blue5 += 2;
		blue5 %= 360;

		drawCopper(GCTemp, 31);

		drect.y = aSin[blue3];
		blue3 += 2;
		blue3 %= 360;

		drawCopper(GCTemp, 31);

		drect.y = aSin[blue];
		blue += 2;
		blue %= 360;

		drawCopper(GCTemp, 31);

		drect.y = aSin[white7];
		white7 += 2;
		white7 %= 360;

		drawCopper(GCTemp, 16);

		drect.y = aSin[white5];
		white5 += 2;
		white5 %= 360;

		drawCopper(GCTemp, 16);

		drect.y = aSin[white3];
		white3 += 2;
		white3 %= 360;

		drawCopper(GCTemp, 16);

		drect.y = aSin[white];
		white += 2;
		white %= 360;

		drawCopper(GCTemp, 16);

		drect.y = aSin[red7];
		red7 += 2;
		red7 %= 360;

		drawCopper(GCTemp, 1);

		drect.y = aSin[red5];
		red5 += 2;
		red5 %= 360;

		drawCopper(GCTemp, 1);

		drect.y = aSin[red3];
		red3 += 2;
		red3 %= 360;

		drawCopper(GCTemp, 1);

		drect.y = aSin[red];
		red += 2;
		red %= 360;

		drawCopper(GCTemp, 1);

		GCTemp.dispose();

		redrawCanvas();
	}

	private void drawCopper(GC gc, int add) {

		for (int i = 0; i < 15; i++) {
			final Color color = new Color(display, colors[i + add]);
			gc.setBackground(color);
			gc.fillRectangle(drect);
			drect.y++;
			color.dispose();
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

		canvas.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event e) {
				w = canvas.getClientArea().width;
				h = canvas.getClientArea().height;
				init();
			}
		});

		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent arg0) {
				redrawCanvas();
			}
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

		final Coppers app = new Coppers(display);

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
