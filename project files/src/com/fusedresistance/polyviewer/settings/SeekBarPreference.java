package com.fusedresistance.polyviewer.settings;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fusedresistance.polyviewer.pro.R;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
	private static final String PREF_NS = "http://schemas.android.com/apk/res/com.fusedresistance.polyviewer";
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
			
	private static final String ATRIB_DEFAULT = "defaultValue";
	private static final String ATRIB_MIN_VAL = "minValue";
	private static final String ATRIB_MAX_VAL = "maxValue";
	
	private static final int DEFAULT_VAL = 50;
	private static final int DEFAULT_MIN_VAL = 0;
	private static final int DEFAULT_MAX_VAL = 100;
	
	private int defaultVal;
	private int minVal;
	private int maxVal;
	private int currVal;
	
	private Context parentContext;
	private SeekBar seekBar;
	private TextView valueTextView;
   private View prefView;
	
	public SeekBarPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		parentContext = context;
		defaultVal    = attrs.getAttributeIntValue(ANDROID_NS, ATRIB_DEFAULT, DEFAULT_VAL);
		minVal        = attrs.getAttributeIntValue(PREF_NS, ATRIB_MIN_VAL, DEFAULT_MIN_VAL);
		maxVal        = attrs.getAttributeIntValue(PREF_NS, ATRIB_MAX_VAL, DEFAULT_MAX_VAL);
		
	}
	
	//@Override
	protected View onCreateDialogView()
	{
		currVal = getPersistedInt(defaultVal);
		
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_slider, null);
		
		((TextView)view.findViewById(R.id.min_value)).setText(Integer.toString(minVal));
		((TextView)view.findViewById(R.id.max_value)).setText(Integer.toString(maxVal));
		
		seekBar = (SeekBar)view.findViewById(R.id.seek_bar);
		seekBar.setMax(maxVal - minVal);
		seekBar.setProgress(currVal - minVal);
		seekBar.setOnSeekBarChangeListener(this);
		
		valueTextView = (TextView)view.findViewById(R.id.current_value);
		valueTextView.setText(Integer.toString(currVal));
		
		return view;
	}
	
	//@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);
		
		if(!positiveResult)
			return;
		
		if(shouldPersist())
			persistInt(currVal);
		
		notifyChanged();
	}
	
	@Override
   protected void onBindView(View view)
   {
      super.onBindView(view);
      
      Typeface ubuntuCondensed = Typeface.createFromAsset(parentContext.getAssets(), "fonts/UbuntuCondensed-Regular.ttf");
      Typeface sourceSansPro   = Typeface.createFromAsset(parentContext.getAssets(), "fonts/SourceSansPro-Regular.ttf");
      TextView titleView       = (TextView)view.findViewById(android.R.id.title);
      TextView summaryView     = (TextView)view.findViewById(android.R.id.summary);
      
      prefView = view;
      prefView.setBackgroundColor(Preferences.DARK_COLOUR);
      
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
   }
	
	//@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser)
	{
		currVal = progress + minVal;
		
		valueTextView.setText(Integer.toString(currVal));
	}

	//@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
	}

	//@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{	
	}
	
	public void setMinMaxValues(int minValue, int maxValue)
	{
		minVal = minValue;
		maxVal = maxValue;
	}
	
	public void setTheme(int styleID, int bgColour)
   {
      if(prefView != null)
         return;
      
      TextView titleView   = (TextView)prefView.findViewById(android.R.id.title);
      TextView summaryView = (TextView)prefView.findViewById(android.R.id.summary);
      
      prefView.setBackgroundColor(bgColour);
      
      if(titleView != null)
         titleView.setTextAppearance(parentContext, styleID);
      
      if(summaryView != null)
         summaryView.setTextAppearance(parentContext, styleID);
   }
}
