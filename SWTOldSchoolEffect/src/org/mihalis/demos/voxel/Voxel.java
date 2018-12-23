/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Christian Hahn (ch@medianetz.de) - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.voxel;

import org.eclipse.swt.SWT;
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

public class Voxel {

	private static final String SHELL_TITLE = "Voxel";

	private static final int CANVAS_WIDTH = 640;
	private static final int CANVAS_HEIGHT = 400;

	// The timer interval in milliseconds
	private static final int TIMER_INTERVAL = 10;

	private Display display;
	private Canvas canvas;
	private GC gc;
	private int w, h;
	private Image image;

	//
	public static final int ANGLE_0 = 0, //
			ANGLE_1 = 5, //
			ANGLE_2 = 10, //
			ANGLE_4 = 20, //
			ANGLE_5 = 25, //
			ANGLE_6 = 30, //
			ANGLE_15 = 80, //
			ANGLE_30 = 160, //
			ANGLE_45 = 240, //
			ANGLE_60 = 320, //
			ANGLE_90 = 480, //
			ANGLE_135 = 720, //
			ANGLE_180 = 960, //
			ANGLE_225 = 1200, //
			ANGLE_270 = 1440, //
			ANGLE_315 = 1680, //
			ANGLE_360 = 1920;

	public final int FIXP_SHIFT = 12; // number of decimal places 20.12
	public final int FIXP_MUL = 4096; // 2^12, used to convert reals
	public final int HFIELD_WIDTH = 512; // width of height field data map
	public final int HFIELD_HEIGHT = 512; // height of height field data map
	public final int HFIELD_BIT_SHIFT = 9; // log base 2 of 512
	public final int TERRAIN_SCALE_X2 = 2; // scaling factor for terrain

	public final int VIEWPLANE_DISTANCE = CANVAS_WIDTH / 64;

	public final int MAX_ALTITUDE = 1000; // maximum and minimum altitudes
	public final int MIN_ALTITUDE = 50;
	public final int MAX_SPEED = 32; // maximum speed of camera

	public final int START_X_POS = 256; // starting viewpoint position
	public final int START_Y_POS = 256;
	public final int START_Z_POS = 700;

	public final int START_PITCH = 80; // starting angular heading
	public final int START_HEADING = ANGLE_180;

	public final int MAX_STEPS = 200; // number of steps to cast ray

	public int cos_look[], sin_look[];
	int heightMapRaw[], colorMapRaw[];
	int virtualScreen[];
	int skyTextureRaw[];

	static int mapWidth, mapHeight;

	boolean cursorKeyUp = false;
	boolean cursorKeyDown = false;
	boolean cursorKeyRight = false;
	boolean cursorKeyLeft = false;
	boolean strafe = false;
	boolean ascend = false;
	boolean descend = false;
	boolean rollleft = false;
	boolean rollright = false;
	int skyColor = 0xff9f0050;
	int skyTextureWidth, skyTextureHeight;

	int dslope = (int) ((double) 1 / (double) VIEWPLANE_DISTANCE * FIXP_MUL);

	// Speed & position
	int speed;
	double dx, dy;

	int view_pos_x, // view point x pos
			view_pos_y, // view point y pos
			view_pos_z, // view point z pos (altitude)
			view_ang_x, // pitch
			view_ang_y, // heading, or yaw
			view_ang_z; // roll, unused

	private boolean listenerAdded = false;

	private PaletteData paletteData;

	public Voxel(Display display) {
		this.display = display;
	}

	public void init() {

		virtualScreen = new int[w * h];
		for (int i = 0; i < w * h; i++) {
			virtualScreen[i] = 0xff000000;
		}

		image = new Image(display, w, h);

		//
		ImageData heightMap, colorMap, skyTexture;
		heightMap = new ImageData(getClass().getResourceAsStream("heightmap1.gif"));
		mapWidth = heightMap.width;
		mapHeight = heightMap.height;

		// Load sky texture
		skyTexture = new ImageData(getClass().getResourceAsStream("sunset1.jpg"));
		skyTextureWidth = 640;
		skyTextureHeight = 99;
		skyTextureRaw = new int[skyTextureWidth * skyTextureHeight];
		skyTexture.getPixels(0, 0, skyTextureWidth * skyTextureHeight, skyTextureRaw, 0);

		// Load colorMap
		colorMapRaw = new int[mapWidth * mapHeight];
		colorMap = new ImageData(getClass().getResourceAsStream("colormap.jpg"));
		colorMap.getPixels(0, 0, mapWidth * mapHeight, colorMapRaw, 0);
		paletteData = colorMap.palette;

		//

		heightMapRaw = new int[mapWidth * mapHeight];
		heightMap.getPixels(0, 0, mapWidth * mapHeight, heightMapRaw, 0);

		for (int i = 0; i < 512 * 512; i++) {
			heightMapRaw[i] = heightMapRaw[i] & 0xff;// we have RBG, but we
			// just want 8 bit range for height mapping
		}

		// Build tables
		cos_look = new int[ANGLE_360];
		sin_look = new int[ANGLE_360];
		for (int curr_angle = 0; curr_angle < ANGLE_360; curr_angle++) {
			final double angle_rad = 2 * Math.PI * curr_angle / ANGLE_360;
			cos_look[curr_angle] = (int) (Math.cos(angle_rad) * FIXP_MUL);
			sin_look[curr_angle] = (int) (Math.sin(angle_rad) * FIXP_MUL);
		}

		// Init position
		dx = 0;
		dy = 0;
		speed = 0;

		view_pos_x = START_X_POS; // view point x pos
		view_pos_y = START_Y_POS; // view point y pos
		view_pos_z = START_Z_POS; // view point z pos (altitude)
		view_ang_x = START_PITCH; // pitch
		view_ang_y = START_HEADING; // heading, or yaw
		view_ang_z = 0; // roll, unused

		if (!listenerAdded) {
			listenerAdded = true;
			canvas.addListener(SWT.KeyDown, new CustomKeyListener(true));
			canvas.addListener(SWT.KeyUp, new CustomKeyListener(false));

		}

		redrawCanvas();
	}

	public void animate() {

		// Clear screen
		ImageData imageData = new ImageData(w, h, 24, paletteData);

		// Go
		int xr, // used to compute the point the ray intersects the
				yr, // the height data
				curr_column, // current screen column being processed
				curr_step, // current step ray is at
				raycast_ang, // current angle of ray being cast
				dx_, dy_, dz_, // general deltas for ray to move from pt to pt
				curr_voxel_scale, // current scaling factor to draw each voxel line
				column_height, // height of the column intersected and being rendered
				curr_row, // number of rows processed in current column
				x_ray, y_ray, z_ray, // the position of the tip of the ray
				map_addr; // temp var used to hold the addr of data bytes
		int dest_buffer = 0, dest_column_ptr = 0;
		int vp_x, vp_y, vp_z;

		if (cursorKeyUp) {
			if (speed < MAX_SPEED) {
				speed += 2;
			}
		}
		if (cursorKeyDown) {
			if (speed > 1) {
				speed -= 2;
			}
		}
		if (cursorKeyLeft) {
			if (strafe) {
				dy = Math.cos(6.28 * view_ang_y / ANGLE_360) * 10;
				dx = -Math.sin(6.28 * view_ang_y / ANGLE_360) * 10;
			} else {
				view_ang_y += ANGLE_5;
			}
		}
		if (cursorKeyRight) {
			if (strafe) {
				dy = -Math.cos(6.28 * view_ang_y / ANGLE_360) * 10;
				dx = Math.sin(6.28 * view_ang_y / ANGLE_360) * 10;
			} else {
				view_ang_y -= ANGLE_5;
			}
		}
		if (ascend) {
			if (view_pos_z < MAX_ALTITUDE) {
				view_pos_z += 8;
			}
		}
		if (descend) {
			if (view_pos_z > MIN_ALTITUDE) {
				view_pos_z -= 8;
			}
		}

		view_pos_x += speed * COS_LOOK(view_ang_y) >> FIXP_SHIFT;
		view_pos_y += speed * SIN_LOOK(view_ang_y) >> FIXP_SHIFT;
		if (view_pos_x >= HFIELD_WIDTH) {
			view_pos_x = 0;
		} else if (view_pos_x < 0) {
			view_pos_x = HFIELD_WIDTH - 1;
		}

		if (view_pos_y >= HFIELD_HEIGHT) {
			view_pos_y = 0;
		} else if (view_pos_y < 0) {
			view_pos_y = HFIELD_HEIGHT - 1;
		}
		if (view_ang_y >= ANGLE_360) {
			view_ang_y -= ANGLE_360;
		} else if (view_ang_y < ANGLE_0) {
			view_ang_y += ANGLE_360;
		}

		if (view_ang_y >= ANGLE_360) {
			view_ang_y -= ANGLE_360;
		} else if (view_ang_y < ANGLE_0) {
			view_ang_y += ANGLE_360;
		}

		for (int y_ = 0; y_ < h; y_++) {
			for (int x_ = 0; x_ < w; x_++) {
				virtualScreen[x_ + y_ * w] = 0;
			}
		}

		// //////////// RENDER TERRAIN:
		final int vp_ang_x = view_ang_x, vp_ang_y = view_ang_y;
		dest_buffer = 0;
		dest_column_ptr = 0;
		vp_x = view_pos_x << FIXP_SHIFT;
		vp_y = view_pos_y << FIXP_SHIFT;
		vp_z = view_pos_z << FIXP_SHIFT;
		dest_buffer += w * (h - 1);
		raycast_ang = vp_ang_y + ANGLE_30;
		for (curr_column = 0; curr_column < w - 1; curr_column++) {
			x_ray = vp_x;
			y_ray = vp_y;
			z_ray = vp_z;
			dx_ = COS_LOOK(raycast_ang) << 1;
			dy_ = SIN_LOOK(raycast_ang) << 1;
			dz_ = dslope * (vp_ang_x - h);
			dest_column_ptr = dest_buffer;
			curr_voxel_scale = 0;
			curr_row = 0;
			for (curr_step = 0; curr_step < MAX_STEPS; curr_step++) {
				xr = x_ray >> FIXP_SHIFT;
				yr = y_ray >> FIXP_SHIFT;
				xr = xr & HFIELD_WIDTH - 1;
				yr = yr & HFIELD_HEIGHT - 1;
				map_addr = xr + (yr << HFIELD_BIT_SHIFT);
				column_height = heightMapRaw[map_addr] << FIXP_SHIFT + TERRAIN_SCALE_X2;
				if (column_height > z_ray) {
					while (true) {
						virtualScreen[dest_column_ptr] = colorMapRaw[map_addr];
						dz_ += dslope;
						z_ray += curr_voxel_scale;
						dest_column_ptr -= w;
						if (++curr_row >= h) {
							curr_step = MAX_STEPS;
							break;
						} // end if
						if (z_ray > column_height) {
							break;
						}
					} // end while
				} // end if
				x_ray += dx_;
				y_ray += dy_;
				z_ray += dz_;
				curr_voxel_scale += dslope;
			} // end for curr_step
			dest_buffer++;
			raycast_ang--;
		} // end for curr_col

		// Draw
		imageData.setPixels(0, 0, w * h, virtualScreen, 0);
		image = null;
		image = new Image(display, imageData);
		imageData = null;

		redrawCanvas();
	}

	public int abs(int x) {
		final int a = x < 0 ? -x : x;
		return a;
	}

	public int COS_LOOK(int theta) {
		if (theta < 0) {
			return cos_look[theta + ANGLE_360];
		} else if (theta >= ANGLE_360) {
			return cos_look[theta - ANGLE_360];
		} else {
			return cos_look[theta];
		}
	}

	public int SIN_LOOK(int theta) {
		if (theta < 0) {
			return sin_look[theta + ANGLE_360];
		} else if (theta >= ANGLE_360) {
			return sin_look[theta - ANGLE_360];
		} else {
			return sin_look[theta];
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
		if (image != null) {
			gc.drawImage(image, 0, 0);
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();

		final Voxel app = new Voxel(display);

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

	class CustomKeyListener implements Listener {

		private boolean keyDown = false;

		/**
		 * Constructeur
		 *
		 * @param keyDown
		 */
		public CustomKeyListener(boolean keyDown) {
			this.keyDown = keyDown;
		}

		@Override
		public void handleEvent(Event event) {
			if (event.keyCode == SWT.ARROW_UP) {
				cursorKeyUp = keyDown;
			}
			if (event.keyCode == SWT.ARROW_DOWN) {
				cursorKeyDown = keyDown;
			}
			if (event.keyCode == SWT.ARROW_LEFT) {
				cursorKeyLeft = keyDown;
			}
			if (event.keyCode == SWT.ARROW_RIGHT) {
				cursorKeyRight = keyDown;
			}
			if (event.keyCode == SWT.PAGE_UP) {
				ascend = keyDown;
			}
			if (event.keyCode == SWT.PAGE_DOWN) {
				descend = keyDown;
			}
			if (event.keyCode == SWT.HOME) {
				strafe = keyDown;
			}
		}
	}
}
