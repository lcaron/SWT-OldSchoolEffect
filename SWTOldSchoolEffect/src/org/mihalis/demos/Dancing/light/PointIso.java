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

public class PointIso {
	private float x, y, z;

	public PointIso() {
	}

	public PointIso(PointIso a, PointIso b, PointIso c, PointIso d) {
		x = (a.x + b.x + c.x + d.x) / 4.0f;
		y = (a.y + b.y + c.y + d.y) / 4.0f;
		z = (a.z + b.z + c.z + d.z) / 4.0f;
	}

	public PointIso(PointIso a, PointIso b) {
		x = b.x - a.x;
		y = b.y - a.y;
		z = b.z - a.z;
	}

	public PointIso(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public float scalar(PointIso p) {
		return x * p.x + y * p.y + z * p.z;
	}

	public void normalize() {
		final float n = norm();
		if (n == 0) {
			return;
		}
		x = x / n;
		y = y / n;
		z = z / n;
	}

	public float norm() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public String toString() {
		return "x=" + x + " y=" + y + " z=" + z;
	}
}
