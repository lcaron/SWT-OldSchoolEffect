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
package org.mihalis.demos.scrolls;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.graphics.ImageData;

/**
 * A filter which performs a perspective distortion on an image. Coordinates are treated as if the image was a unit square, i.e. the bottom-right corner of the image is at (1, 1). The filter maps the unit square onto an arbitrary convex quadrilateral or
 * vice versa.
 */
public class PerspectiveFilter extends TransformFilter {

	private float x0, y0, x1, y1, x2, y2, x3, y3;
	private float dx1, dy1, dx2, dy2, dx3, dy3;
	private float A, B, C, D, E, F, G, H, I;
	private float a11, a12, a13, a21, a22, a23, a31, a32, a33;
	private boolean scaled;
	private boolean clip = false;

	/**
	 * Construct a PerspectiveFilter.
	 */
	public PerspectiveFilter() {
		this(0, 0, 1, 0, 1, 1, 0, 1);
	}

	/**
	 * Construct a PerspectiveFilter.
	 *
	 * @param x0 the new position of the top left corner
	 * @param y0 the new position of the top left corner
	 * @param x1 the new position of the top right corner
	 * @param y1 the new position of the top right corner
	 * @param x2 the new position of the bottom right corner
	 * @param y2 the new position of the bottom right corner
	 * @param x3 the new position of the bottom left corner
	 * @param y3 the new position of the bottom left corner
	 */
	public PerspectiveFilter(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		unitSquareToQuad(x0, y0, x1, y1, x2, y2, x3, y3);
	}

	public void setClip(boolean clip) {
		this.clip = clip;
	}

	public boolean getClip() {
		return clip;
	}

	/**
	 * Set the new positions of the image corners. This is the same as unitSquareToQuad, but the coordinates are in image pixels, not relative to the unit square. This method is provided as a convenience.
	 *
	 * @param x0 the new position of the top left corner
	 * @param y0 the new position of the top left corner
	 * @param x1 the new position of the top right corner
	 * @param y1 the new position of the top right corner
	 * @param x2 the new position of the bottom right corner
	 * @param y2 the new position of the bottom right corner
	 * @param x3 the new position of the bottom left corner
	 * @param y3 the new position of the bottom left corner
	 */
	public void setCorners(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		unitSquareToQuad(x0, y0, x1, y1, x2, y2, x3, y3);
		scaled = true;
	}

	/**
	 * Set the transform to map the unit square onto a quadrilateral. When filtering, all coordinates will be scaled by the size of the image.
	 *
	 * @param x0 the new position of the top left corner
	 * @param y0 the new position of the top left corner
	 * @param x1 the new position of the top right corner
	 * @param y1 the new position of the top right corner
	 * @param x2 the new position of the bottom right corner
	 * @param y2 the new position of the bottom right corner
	 * @param x3 the new position of the bottom left corner
	 * @param y3 the new position of the bottom left corner
	 */
	public void unitSquareToQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.x3 = x3;
		this.y3 = y3;

		dx1 = x1 - x2;
		dy1 = y1 - y2;
		dx2 = x3 - x2;
		dy2 = y3 - y2;
		dx3 = x0 - x1 + x2 - x3;
		dy3 = y0 - y1 + y2 - y3;

		if (dx3 == 0 && dy3 == 0) {
			a11 = x1 - x0;
			a21 = x2 - x1;
			a31 = x0;
			a12 = y1 - y0;
			a22 = y2 - y1;
			a32 = y0;
			a13 = a23 = 0;
		} else {
			a13 = (dx3 * dy2 - dx2 * dy3) / (dx1 * dy2 - dy1 * dx2);
			a23 = (dx1 * dy3 - dy1 * dx3) / (dx1 * dy2 - dy1 * dx2);
			a11 = x1 - x0 + a13 * x1;
			a21 = x3 - x0 + a23 * x3;
			a31 = x0;
			a12 = y1 - y0 + a13 * y1;
			a22 = y3 - y0 + a23 * y3;
			a32 = y0;
		}
		a33 = 1;
		scaled = false;
	}

	/**
	 * Set the transform to map a quadrilateral onto the unit square. When filtering, all coordinates will be scaled by the size of the image.
	 *
	 * @param x0 the old position of the top left corner
	 * @param y0 the old position of the top left corner
	 * @param x1 the old position of the top right corner
	 * @param y1 the old position of the top right corner
	 * @param x2 the old position of the bottom right corner
	 * @param y2 the old position of the bottom right corner
	 * @param x3 the old position of the bottom left corner
	 * @param y3 the old position of the bottom left corner
	 */
	public void quadToUnitSquare(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		unitSquareToQuad(x0, y0, x1, y1, x2, y2, x3, y3);

		// Invert the transformation
		final float ta11 = a22 * a33 - a32 * a23;
		final float ta21 = a32 * a13 - a12 * a33;
		final float ta31 = a12 * a23 - a22 * a13;
		final float ta12 = a31 * a23 - a21 * a33;
		final float ta22 = a11 * a33 - a31 * a13;
		final float ta32 = a21 * a13 - a11 * a23;
		final float ta13 = a21 * a32 - a31 * a22;
		final float ta23 = a31 * a12 - a11 * a32;
		final float ta33 = a11 * a22 - a21 * a12;
		final float f = 1.0f / ta33;

		a11 = ta11 * f;
		a21 = ta12 * f;
		a31 = ta13 * f;
		a12 = ta21 * f;
		a22 = ta22 * f;
		a32 = ta23 * f;
		a13 = ta31 * f;
		a23 = ta32 * f;
		a33 = 1.0f;
	}

	@Override
	public ImageData filter(ImageData src, ImageData dst) {
		A = a22 * a33 - a32 * a23;
		B = a31 * a23 - a21 * a33;
		C = a21 * a32 - a31 * a22;
		D = a32 * a13 - a12 * a33;
		E = a11 * a33 - a31 * a13;
		F = a31 * a12 - a11 * a32;
		G = a12 * a23 - a22 * a13;
		H = a21 * a13 - a11 * a23;
		I = a11 * a22 - a21 * a12;
		if (!scaled) {
			final int width = src.width;
			final int height = src.height;
			final float invWidth = 1.0f / width;
			final float invHeight = 1.0f / height;

			A *= invWidth;
			D *= invWidth;
			G *= invWidth;
			B *= invHeight;
			E *= invHeight;
			H *= invHeight;
		}

		return super.filter(src, dst);
	}

	@Override
	protected void transformSpace(Rectangle rect) {
		if (scaled) {
			rect.x = (int) Math.min(Math.min(x0, x1), Math.min(x2, x3));
			rect.y = (int) Math.min(Math.min(y0, y1), Math.min(y2, y3));
			rect.width = (int) Math.max(Math.max(x0, x1), Math.max(x2, x3)) - rect.x;
			rect.height = (int) Math.max(Math.max(y0, y1), Math.max(y2, y3)) - rect.y;
			return;
		}
		if (!clip) {
			final float w = (float) rect.getWidth(), h = (float) rect.getHeight();
			final Rectangle r = new Rectangle();
			r.add(getPoint2D(new Point2D.Float(0, 0), null));
			r.add(getPoint2D(new Point2D.Float(w, 0), null));
			r.add(getPoint2D(new Point2D.Float(0, h), null));
			r.add(getPoint2D(new Point2D.Float(w, h), null));
			rect.setRect(r);
		}
	}

	/**
	 * Get the origin of the output image. Use this for working out where to draw your new image.
	 *
	 * @return the X origin.
	 */
	public float getOriginX() {
		return x0 - (int) Math.min(Math.min(x0, x1), Math.min(x2, x3));
	}

	/**
	 * Get the origin of the output image. Use this for working out where to draw your new image.
	 *
	 * @return the Y origin.
	 */
	public float getOriginY() {
		return y0 - (int) Math.min(Math.min(y0, y1), Math.min(y2, y3));
	}

	public Rectangle2D getBounds2D(ImageData src) {
		if (clip) {
			return new Rectangle(0, 0, src.width, src.height);
		}
		final float w = src.width, h = src.height;
		final Rectangle2D r = new Rectangle2D.Float();
		r.add(getPoint2D(new Point2D.Float(0, 0), null));
		r.add(getPoint2D(new Point2D.Float(w, 0), null));
		r.add(getPoint2D(new Point2D.Float(0, h), null));
		r.add(getPoint2D(new Point2D.Float(w, h), null));
		return r;
	}

	public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
		if (dstPt == null) {
			dstPt = new Point2D.Float();
		}
		final float x = (float) srcPt.getX();
		final float y = (float) srcPt.getY();
		final float f = 1.0f / (x * a13 + y * a23 + a33);
		dstPt.setLocation((x * a11 + y * a21 + a31) * f, (x * a12 + y * a22 + a32) * f);
		return dstPt;
	}

	@Override
	protected void transformInverse(int x, int y, float[] out) {
		out[0] = originalSpace.width * (A * x + B * y + C) / (G * x + H * y + I);
		out[1] = originalSpace.height * (D * x + E * y + F) / (G * x + H * y + I);
	}

	@Override
	public String toString() {
		return "Distort/Perspective...";
	}
}
