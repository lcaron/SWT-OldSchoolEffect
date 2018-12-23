/*******************************************************************************
 * Copyright (c) 2018 Laurent Caron
 *
 * All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Guillaume Bouchon (bouchon_guillaume@yahoo.fr) - Original Version
 * Laurent CARON (laurent.caron at gmail dot com) - Conversion to SWT
 *******************************************************************************/
package org.mihalis.demos.Dancing.light;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class PlanIso {

	private final Image image;
	private final int width;
	private final int height;
	private int center_x;
	private int center_y;
	private final int cw;
	private final int ch;
	private final PointIso points[][];
	private final int pw;
	private final int ph;
	private double dec = 0.0;
	private final int dx;
	private final int wave_cx;
	private final int wave_cy;
	private final double wave_freq = 1.0;
	private final PointIso light;
	private double decl = 0.0;
	private final float light_r = 255;
	private final float light_g = 255;
	private final float light_b = 0;
	private final double dist_attenuation = 45.0;
	private final int xc;
	private final int plan_r = 0;
	private final int plan_g = 0;
	private final int plan_b = 0;

	public PlanIso(int w, int h, int cw, int ch, int dx) {
		width = w;
		height = h;
		this.cw = cw;
		this.ch = ch;
		this.dx = dx;

		pw = w / cw;
		ph = h / ch;

		wave_cx = pw / 2;
		wave_cy = ph / 2;

		points = new PointIso[pw][ph];
		for (int x = 0; x < pw; x++) {
			for (int y = 0; y < ph; y++) {
				points[x][y] = new PointIso();
			}
		}

		image = new Image(Display.getCurrent(), w, h);

		light = new PointIso(center_x, center_y, 10);

		xc = ph / 2 * dx;

		center_x = cw * pw / 2 + xc;
		center_y = ch * ph / 2;

		next();
	}

	public void next() {
		dec += 0.25;
		decl += 0.05;

		light.setY((float) (center_y + Math.cos(decl) * (height / 2.0)));
		light.setX((float) (center_x + Math.sin(decl) * (width / 2.0)));

		for (int x = 0; x < pw; x++) {
			for (int y = 0; y < ph; y++) {
				points[x][y].setX(x * cw + y * dx);

				points[x][y].setZ((float) (-Math.sin(dec + wave_freq * Math.sqrt((x - wave_cx) * (x - wave_cx) + (y - wave_cy) * (y - wave_cy)) + ch) * dx));
				points[x][y].setY(y * ch - points[x][y].getZ());
			}
		}
	}

	public Image render() {
		if (image == null) {
			return null;
		}

		GC gc;
		try {
			gc = new GC(image);
		} catch (final IllegalArgumentException iae) {
			return null;
		}

		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 0, width, height);

		gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

		final int p[] = new int[8];

		for (int x = 0; x < pw - 1; x++) {
			for (int y = 0; y < ph - 1; y++) {
				final PointIso c = new PointIso(points[x][y], points[x + 1][y], points[x + 1][y + 1], points[x][y + 1]);
				final PointIso v = new PointIso(c, light);
				final PointIso u = new PointIso(points[x][y], points[x][y + 1]);

				final float dist = v.norm();

				u.normalize();
				v.normalize();

				final double a = Math.abs(Math.acos(u.scalar(v)));

				int colr = plan_r, colg = plan_g, colb = plan_b;

				colr = Math.min(255, (int) (colr + (Math.PI - a) * light_r / (dist / dist_attenuation)));
				colr = Math.max(0, colr);

				colg = Math.min(255, (int) (colg + (Math.PI - a) * light_g / (dist / dist_attenuation)));
				colg = Math.max(0, colg);

				colb = Math.min(255, (int) (colb + (Math.PI - a) * light_b / (dist / dist_attenuation)));
				colb = Math.max(0, colb);

				final Color color = new Color(Display.getCurrent(), colr, colg, colb);

				p[0] = (int) (points[x][y].getX() - xc);
				p[2] = (int) (points[x + 1][y].getX() - xc);
				p[4] = (int) (points[x + 1][y + 1].getX() - xc);
				p[6] = (int) (points[x][y + 1].getX() - xc);

				p[1] = (int) points[x][y].getY();
				p[3] = (int) points[x + 1][y].getY();
				p[5] = (int) points[x + 1][y + 1].getY();
				p[7] = (int) points[x][y + 1].getY();

				gc.setBackground(color);
				gc.fillPolygon(p);
				color.dispose();
			}
		}
		gc.dispose();
		return image;
	}
}
