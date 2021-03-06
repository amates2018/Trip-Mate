/*
 * Copyright (c) 2018. Property of Dennis Kwabena Bilson. No unauthorized duplication of this material should be made without prior permission from the developer
 */

package io.peanutsdk.recyclerview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridMarginDecoration extends RecyclerView.ItemDecoration {
	
	private int space;
	
	public GridMarginDecoration(int space) {
		this.space = space;
	}
	
	@Override
	public void getItemOffsets(Rect outRect, View view,
	                           RecyclerView parent, RecyclerView.State state) {
		outRect.left = space;
		outRect.top = space;
		outRect.right = space;
		outRect.bottom = space;
	}
}

