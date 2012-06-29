package com.android.fanfou;


import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
 
public class MyAdapter extends SimpleAdapter {
 
	private int[] mTo;
    private String[] mFrom;
    private ViewBinder mViewBinder;
 
    private List<? extends Map<String, ?>> mData;
 
    private int mResource;
    private int mDropDownResource;
    private LayoutInflater mInflater;
 
	public MyAdapter(Context context,List<? extends Map<String, ?>> data, int resource, String[] from,int[] to) {
		super(context, data, resource, from, to);
		mData = data;
        mResource = mDropDownResource = resource;
        mFrom = from;
        mTo = to;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
 
 
	 /**
     * @see android.widget.Adapter#getView(int, View, ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }
 
    private View createViewFromResource(int position, View convertView,
            ViewGroup parent, int resource) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(resource, parent, false);
 
            final int[] to = mTo;
            final int count = to.length;
            final View[] holder = new View[count];
 
            for (int i = 0; i < count; i++) {
                holder[i] = v.findViewById(to[i]);
            }
 
            v.setTag(holder);
        } else {
            v = convertView;
        }
 
        bindView(position, v);
 
        return v;
    }
 
    private void bindView(int position, View view) {
        final Map dataSet = mData.get(position);
        if (dataSet == null) {
            return;
        }
 
        final ViewBinder binder = mViewBinder;
        final View[] holder = (View[]) view.getTag();
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;
 
        for (int i = 0; i < count; i++) {
            final View v = holder[i];
            if (v != null) {
                final Object data = dataSet.get(from[i]);
//                if(data i)
                String text = data == null ? "" : data.toString();
                if (text == null) {
                    text = "";
                }
 
                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }
 
                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() +
                                    " should be bound to a Boolean, not a " + data.getClass());
                        }
                    } else if (v instanceof TextView) {
                        // Note: keep the instanceof TextView check at the bottom of these
                        // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
 
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);                            
                        } else if(data instanceof Bitmap) {
                            setViewImage((ImageView) v, (Bitmap)data);
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }
 
    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }
 
 
    public void setViewImage(ImageView v, Bitmap bm) {
    	((ImageView) v).setImageBitmap(bm);
    }
 
}