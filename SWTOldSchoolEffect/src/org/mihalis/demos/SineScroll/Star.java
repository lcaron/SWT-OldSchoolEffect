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

public class Star {
	public int x, y, dx, color;

	public Star(int w, int h) {
		x = (int) (Math.random() * w);
		y = (int) (Math.random() * h);
		dx = (int) (Math.random() * 4.0) + 1;
		color = ((int) (Math.random() * 11.0) + 4) * 0x111111;
	}
}
