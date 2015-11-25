package com.fusedresistance.polyviewer.colorpicker;

import com.fusedresistance.polyviewer.colorpicker.ColourPicker.OnColorChangedListener;
import com.fusedresistance.polyviewer.pro.R;
import com.fusedresistance.polyviewer.settings.Preferences;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColourPickerPreference 
	extends Preference
	implements Preference.OnPreferenceClickListener, OnColorChangedListener
{
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	
	View view;
	int defaultValue = Color.BLACK;
	
	private int value = Color.BLACK;
	private float density = 0.0f;

   private Context parentContext;
	
	public ColourPickerPreference(Context context) 
	{
		super(context);
		init(context, null);
	}
	
	public ColourPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ColourPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs)
	{
		density = getContext().getResources().getDisplayMetrics().density;
		
		parentContext = context;
		
		setOnPreferenceClickListener(this);
		
		if (attrs != null) 
		{
			String strValue = attrs.getAttributeValue(androidns, "defaultValue");
			
			if(strValue.startsWith("#")) 
			{
				try
				{
					defaultValue = colourStrToInt(strValue);
				}
				catch (NumberFormatException e) {
					Log.e("ColorPickerPreference", "Wrong color: " + defaultValue);
					defaultValue = colourStrToInt("#FF000000");
				}
			} 
			else
			{
				int resourceId = attrs.getAttributeResourceValue(androidns, "defaultValue", 0);
				
				if (resourceId != 0)
				{
					defaultValue = context.getResources().getInteger(resourceId);
				}
			}
		}
		
		value = defaultValue;
	}
	
	@Override
	protected void onBindView(View newView)
	{
		super.onBindView(newView);
		view = newView;
		
		Typeface ubuntuCondensed = Typeface.createFromAsset(parentContext.getAssets(), "fonts/UbuntuCondensed-Regular.ttf");
      Typeface sourceSansPro   = Typeface.createFromAsset(parentContext.getAssets(), "fonts/SourceSansPro-Regular.ttf");
      TextView titleView       = (TextView)view.findViewById(android.R.id.title);
      TextView summaryView     = (TextView)view.findViewById(android.R.id.summary);
      
      view.setBackgroundColor(Preferences.DARK_COLOUR);
      
      if(titleView != null)
      {
         titleView.setTypeface(ubuntuCondensed);
         titleView.setTextAppearance(parentContext, R.style.PolyPreference);
      }
      
      if(summaryView != null)
      {
         summaryView.setTypeface(sourceSansPro);
         summaryView.setTextAppearance(parentContext, R.style.PolyPreference);
      }
		
		setPreviewColour();
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) 
	{
		ColourPicker picker = new ColourPicker(getContext(), getValue());
		
		picker.setOnColorChangedListener(this);
		picker.show();

		return false;
	}
	
	public void onColourChanged(int colour)
	{
		if(isPersistent())
			persistInt(colour);
		
		value = colour;
		
		setPreviewColour();
		
		try 
		{
			getOnPreferenceChangeListener().onPreferenceChange(this, colour);
		} 
		catch (NullPointerException e) {}
		
	}
	
	private void setPreviewColour()
	{
		if (view == null)
			return;
		
		ImageView iView = new ImageView(getContext());
		LinearLayout widgetFrameView = ((LinearLayout)view.findViewById(android.R.id.widget_frame));
		
		if (widgetFrameView == null) 
			return;
		
		widgetFrameView.setVisibility(View.VISIBLE);
		widgetFrameView.setPadding(widgetFrameView.getPaddingLeft(),
								   widgetFrameView.getPaddingTop(),
								   (int)(density * 8),
								   widgetFrameView.getPaddingBottom());
		
		// remove already create preview image
		int count = widgetFrameView.getChildCount();
		
		if (count > 0) 
			widgetFrameView.removeViews(0, count);
		
		widgetFrameView.addView(iView);
		iView.setImageBitmap(getPreviewBitmap());
	}
	
	private Bitmap getPreviewBitmap() 
	{
		int d = (int) (density * 31);
		int color = getValue();
		Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
		int w = bm.getWidth();
		int h = bm.getHeight();
		int c = color;
		for (int i = 0; i < w; i++)
		{
			for (int j = i; j < h; j++)
			{
				c = (i <= 1 || j <= 1 || i >= w-2 || j >= h-2) ? Color.GRAY : color;
				bm.setPixel(i, j, c);
				
				if (i != j)
					bm.setPixel(j, i, c);
				
			}
		}

		return bm;
	}
	
	public int getValue() 
	{
		try 
		{
			if(isPersistent())
				value = getPersistedInt(defaultValue);
		}
		catch (ClassCastException e) 
		{
			value = defaultValue;
		}

		return value;
	}
	
	public static int colourStrToInt(String argb) throws NumberFormatException
	{
		if (argb.startsWith("#"))
    		argb = argb.replace("#", "");
    	
        int alpha = -1;
        int red = -1;
        int green = -1;
		int blue = -1;

        if (argb.length() == 8)
        {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        }
        else if (argb.length() == 6) 
        {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }

	@Override
	public void onColorChanged(int colour)
	{
		if (isPersistent()) {
			persistInt(colour);
		}
		value = colour;
		setPreviewColour();
		
		try
		{
			getOnPreferenceChangeListener().onPreferenceChange(this, colour);
		}
		catch (NullPointerException e) {}
		
	}

	public static String convertToARGB(Integer colour) 
	{
		String alpha = Integer.toHexString(Color.alpha(colour));
        String red = Integer.toHexString(Color.red(colour));
        String green = Integer.toHexString(Color.green(colour));
        String blue = Integer.toHexString(Color.blue(colour));

        if (alpha.length() == 1)
            alpha = "0" + alpha;

        if (red.length() == 1)
            red = "0" + red;

        if (green.length() == 1)
            green = "0" + green;

        if (blue.length() == 1)
            blue = "0" + blue;

        return "#" + alpha + red + green + blue;
	}
	   
   public void setTheme(int styleID, int bgColour)
   {
      if(view != null)
         return;
      
      TextView titleView   = (TextView)view.findViewById(android.R.id.title);
      TextView summaryView = (TextView)view.findViewById(android.R.id.summary);
      
      view.setBackgroundColor(bgColour);
      
      if(titleView != null)
         titleView.setTextAppearance(parentContext, styleID);
      
      if(summaryView != null)
         summaryView.setTextAppearance(parentContext, styleID);
   }
}
