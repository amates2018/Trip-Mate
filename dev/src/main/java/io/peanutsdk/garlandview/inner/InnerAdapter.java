/*
 * Copyright (c) 2018. Property of Dennis Kwabena Bilson. No unauthorized duplication of this material should be made without prior permission from the developer
 */

package io.peanutsdk.garlandview.inner;


import android.support.v7.widget.RecyclerView;

/**
 * Adapter class for {@link InnerRecyclerView}.
 * @param <T> inner item class.
 */
public abstract class InnerAdapter<T extends InnerItem> extends RecyclerView.Adapter<T> {}