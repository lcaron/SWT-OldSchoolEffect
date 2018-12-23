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
package org.mihalis.demos.SineScroll;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class SineScroll {

	private short font[];
	private byte fontLookup[];
	private int scrollPos, scrollOffset, sinePos;
	private int[] background;
	private byte scrollText[];
	private int rasters[];
	private int sine[];
	Star[] stars;
	boolean haveStars;
	int bgColor;
	int scrPix[];

	// Title
	private static final String SHELL_TITLE = "Sine Scroll";

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private final Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private ImageData imageData;

	public SineScroll(final Display display) {
		this.display = display;
	}

	public void init() {

		// Init
		if (!initFont()) {
			System.err.println("Impossible to load the fonts");
			System.exit(1);
		}
		initBackground();
		initScrollText("This is an old-fashion amiga demo !");
		initRasters();
		initSine();
		initStars("0", "250");

		scrPix = new int[w * h];
		scrollOffset = 0;
		scrollPos = 0;
		sinePos = 0;

		imageData = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));

		redrawCanvas();
	}

	public void animate() {
		// Animation
		// obtain current scroll offset/text position and sine position,
		// then advance all of them.
		// scroll offset runs from 0 to 15. when it reaches 15, the
		// scroll position (the scrolltext character to be drawn) advances
		// and the scroll offset goes back to 0.
		int sco = scrollOffset, scp = scrollPos, sp = sinePos;
		if (++scrollOffset >= 16) {
			scrollOffset = 0;
			if (++scrollPos >= scrollText.length) {
				scrollPos = 0;
			}
		}
		sinePos += 4;
		if (sinePos >= sine.length) {
			sinePos -= sine.length;
		}

		// clear screen by copying background
		System.arraycopy(background, 0, scrPix, 0, scrPix.length);

		// if we have a starfield, draw it
		if (stars != null) {
			for (int i = 0; i < stars.length; i++) {
				final Star s = stars[i];
				while (s.x >= w) {
					s.x -= w;
				}
				// stars are only drawn over the bgcolor
				if ((scrPix[s.y * w + s.x] & 0xFFFFFF) == bgColor) {
					scrPix[s.y * w + s.x] = s.color;
				}
				s.x += s.dx;
			}
		}

		// draw a series of 16-pixel high vertical lines
		for (int x = 0; x < w; x++) {
			final int ypos = sine[sp++];
			if (sp >= sine.length) {
				sp = 0;
			}

			final short data = font[scrollText[scp] * 16 + sco];
			if (++sco >= 16) {
				sco = 0;
				if (++scp >= scrollText.length) {
					scp = 0;
				}
			}

			for (int y = 0; y < 16; y++) {
				if ((data & 1 << y) != 0) {
					scrPix[(y + ypos) * w + x] = rasters[y + ypos];
					// scroll shadow
					if (y + ypos + 2 < w && x + 2 < w) {
						scrPix[(y + ypos + 2) * w + x + 2] = 0x000000;
					}
				}
			}
		}

		imageData.setPixels(0, 0, scrPix.length, scrPix, 0);

		redrawCanvas();
	}

	private Shell createWindow() {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(SHELL_TITLE);
		shell.setLayout(new GridLayout(1, false));

		canvas = new Canvas(shell, SWT.BORDER | SWT.NO_REDRAW_RESIZE);
		final GridData gdCanvas = new GridData(GridData.FILL, GridData.FILL, true, true);
		gdCanvas.widthHint = 256;
		gdCanvas.heightHint = 256;
		canvas.setLayoutData(gdCanvas);

		gc = new GC(canvas);

		canvas.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				w = canvas.getClientArea().width;
				h = canvas.getClientArea().height;
				init();
			}
		});

		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(final PaintEvent arg0) {
				redrawCanvas();
			}
		});

		return shell;
	}

	private void redrawCanvas() {
		if (imageData == null) {
			return;
		}
		final Image img = new Image(display, imageData);
		if (img != null) {
			gc.drawImage(img, 0, 0);
		}
		img.dispose();

	}

	public static void main(final String[] args) {
		final Display display = new Display();

		final SineScroll app = new SineScroll(display);

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

	// loads the classic 16x16 font into font[]
	// classic_16x16 has 46 glyphs:
	// !'(),-.:?0123456789abcdefghijklmnopqrstuvwxyz
	private boolean initFont() {
		// set up the font lookup table
		// fontLookup[character] = glyph number
		fontLookup = new byte[256];
		final String s = " !'(),-.:?0123456789abcdefghijklmnopqrstuvwxyz";
		final char sc[] = s.toCharArray();
		for (int i = 0; i < 256; i++) {
			fontLookup[i] = 0; // space
		}
		for (int i = 0; i < sc.length; i++) {
			fontLookup[(byte) sc[i]] = (byte) i;
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			// also do A-Z, not just a-z
			fontLookup[c] = (byte) (c - 'A' + 20);
		}

		// grab a stream of 1x16 slivers, from top to bottom, left to right.
		// every 16 of these slivers is one font glyph.
		final Image img = new Image(Display.getCurrent(), "C:/perso/eclipse/workspace/Demos/src/org/mihalis/demos/SineScroll/font_classic_16x16.gif");

		final int w = img.getBounds().width;

		int xpos = 0, ypos = 0;
		font = new short[46 * 16];
		for (int i = 0; i < font.length; i++) {
			if (xpos >= w) {
				xpos = 0;
				ypos += 16;
			}
			short data = 0;
			for (int y = 0; y < 16; y++) {
				final int pixel = img.getImageData().getPixel(xpos, ypos + y);
				if ((pixel & 0xFFFFFF) != 0) {
					data |= 1 << y;
				}
			}
			font[i] = data;
			xpos++;
		}
		return true;
	}

	private void initBackground() {
		background = new int[w * h];
		for (int i = 0; i < background.length; i++) {
			background[i] = 0x000000;
		}
	}

	// creates scrollText[], which is an array of glyph lookups rather
	// than ascii text
	private void initScrollText(String text) {
		if (text == null) {
			text = "no scrolltext! ";
		}
		text = "                    " + text;
		final char chrs[] = text.toCharArray();
		scrollText = new byte[chrs.length];
		for (int i = 0; i < chrs.length; i++) {
			scrollText[i] = fontLookup[chrs[i]];
		}
	}

	// a simple set of colour fades down the screen
	private void initRasters() {
		final int cols[] = { 0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0xFF00FF, 0x00FFFF, 0x0000FF };
		int bar = 0;
		final int seg = h / (cols.length - 1);

		rasters = new int[h];

		int r_from, g_from, b_from, r_to, g_to, b_to;
		r_from = cols[0] >> 16 & 0xFF;
		g_from = cols[0] >> 8 & 0xFF;
		b_from = cols[0] & 0xFF;

		for (int x = 1; x <= cols.length - 1; x++) {
			r_to = cols[x] >> 16 & 0xFF;
			g_to = cols[x] >> 8 & 0xFF;
			b_to = cols[x] & 0xFF;
			for (int y = 0; y < seg; y++) {
				int r, g, b;
				r = r_from + (r_to - r_from) * (y + 1) / seg;
				g = g_from + (g_to - g_from) * (y + 1) / seg;
				b = b_from + (b_to - b_from) * (y + 1) / seg;
				rasters[bar++] = r << 16 | g << 8 | b;
			}
			r_from = r_to;
			g_from = g_to;
			b_from = b_to;
		}

	}

	// we use a lookup table for the sine wave rather than calculate it in
	// realtime
	private void initSine() {
		sine = new int[1024];
		// 0-1024 will be for us 0 to 4pi (720 degrees)
		// 1024 * x = 4pi, x=4pi/1024 = pi/(1024/4) = pi/256
		//
		// the amplitude of the wave the screen height - 2, so there's a 1
		// pixel border top and bottom. But we also have to include the size
		// of the font, so the centre line running through the font has an
		// amplitude of screen height - 18 (16 pixel high font).
		final double ampl = (h - 18) / 2;
		for (int i = 0; i < sine.length; i++) {
			sine[i] = (int) (Math.sin(i * 0.50 * Math.PI / 256.0) * Math.sin(i * 0.75 * Math.PI / 256.0) * ampl + ampl) + 1;
		}

	}

	private void initStars(final String bgColor_t, final String numstars_t) {
		bgColor = 0x000000;
		if (bgColor_t != null) {
			try {
				bgColor = Integer.parseInt(bgColor_t);
			} catch (final Exception e) {
			}
		}

		stars = null;
		if (numstars_t != null) {
			try {
				final int numstars = Integer.parseInt(numstars_t);
				if (numstars > 0) {
					stars = new Star[numstars];
				}
			} catch (final Exception e) {
			}
		}

		if (stars != null) {
			for (int i = 0; i < stars.length; i++) {
				stars[i] = new Star(w, h);
			}
		}
	}

}
