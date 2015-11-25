package com.fusedresistance.polyviewer.colorpicker;

import com.fusedresistance.polyviewer.pro.R;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

public class ColourPicker 
	extends Dialog
	implements View.OnClickListener, SeekBar.OnSeekBarChangeListener
{
	private View pickerView = null;
	private View colourPanel = null;
	private SeekBar redBar = null;
	private SeekBar greenBar = null;
	private SeekBar blueBar = null;
	private EditText redText = null;
	private EditText greenText = null;
	private EditText blueText = null;
	private Button cancelButton = null;
	private Button okayButton = null;
	
	private int redValue = 0;
	private int greenValue = 0;
	private int blueValue = 0;
	
	private Context parentContext;
	
	private OnColorChangedListener colourListener;
	
	public interface OnColorChangedListener 
	{
		public void onColorChanged(int color);
	}
	
	public ColourPicker(Context context, int initialColor)
	{
		super(context);
		
		parentContext = context;
		
		init(initialColor);
	}

	private void init(int initialColour) 
	{
		LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		pickerView = inflator.inflate(R.layout.color_picker_dialog, null);
		
		redValue = Color.red(initialColour);
		greenValue = Color.green(initialColour);
		blueValue = Color.blue(initialColour);
		
		setContentView(pickerView);
		setTitle("Colour Picker");
		
		colourPanel = pickerView.findViewById(R.id.colour_panel);
		
		redBar = (SeekBar)pickerView.findViewById(R.id.redSeekBar);
		greenBar = (SeekBar)pickerView.findViewById(R.id.greenSeekBar);
		blueBar = (SeekBar)pickerView.findViewById(R.id.blueSeekBar);
		
		redText = (EditText)pickerView.findViewById(R.id.redTextValue);
		greenText = (EditText)pickerView.findViewById(R.id.greenTextValue);
		blueText = (EditText)pickerView.findViewById(R.id.blueTextValue);
		
		cancelButton = (Button)pickerView.findViewById(R.id.colour_dialog_cancel);
		okayButton = (Button)pickerView.findViewById(R.id.colour_dialog_okay);
		
		redBar.setOnSeekBarChangeListener(this);
		redBar.setProgress(redValue);
		
		greenBar.setOnSeekBarChangeListener(this);
		greenBar.setProgress(greenValue);
		
		blueBar.setOnSeekBarChangeListener(this);
		blueBar.setProgress(blueValue);		
		
		Typeface ubuntuCondensed = Typeface.createFromAsset(parentContext.getAssets(), "fonts/UbuntuCondensed-Regular.ttf");
      Typeface sourceSansPro   = Typeface.createFromAsset(parentContext.getAssets(), "fonts/SourceSansPro-Regular.ttf");
		
		TextView tempView = (TextView)pickerView.findViewById(R.id.redText);
		tempView.setTypeface(ubuntuCondensed);
		
		tempView = (TextView)pickerView.findViewById(R.id.greenText);
      tempView.setTypeface(ubuntuCondensed);
      
      tempView = (TextView)pickerView.findViewById(R.id.blueText);
      tempView.setTypeface(ubuntuCondensed);
      
      okayButton.setTypeface(ubuntuCondensed);
      cancelButton.setTypeface(ubuntuCondensed);
		
      redText.setTypeface(sourceSansPro);
      greenText.setTypeface(sourceSansPro);
      blueText.setTypeface(sourceSansPro);
      
		redText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void afterTextChanged(Editable s)
				{
					try
					{
						redValue = Integer.parseInt(s.toString());
						redBar.setProgress(redValue);
	
						updateColourView();
					}
					catch(Exception e) {}
				}
	
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
			});
		
		greenText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void afterTextChanged(Editable s)
				{
					try
					{
						greenValue = Integer.parseInt(s.toString());
						greenBar.setProgress(greenValue);
						
						updateColourView();
					}
					catch(Exception e) {}
				}
	
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
			});
		
		blueText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void afterTextChanged(Editable s)
				{
					try
					{
						blueValue = Integer.parseInt(s.toString());
						blueBar.setProgress(blueValue);
						
						updateColourView();
					}
					catch(Exception e) {}
				}
	
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
			});
		
		cancelButton.setOnClickListener(this);
		okayButton.setOnClickListener(this);
		
		updateColourView();
	}
	
	public void setOnColorChangedListener(OnColorChangedListener listener)
	{
		colourListener = listener;
	}

	@Override
	public void onClick(View view)
	{
		if (view.getId() == R.id.colour_dialog_okay)
		{
			if (colourListener != null) 
				colourListener.onColorChanged(Color.rgb(redValue, greenValue, blueValue));
		}
		
		dismiss();
	}
	
	@Override
    public void onStopTrackingTouch(SeekBar seekBar) 
    {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) 
    {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) 
    {
    	switch(seekBar.getId())
    	{
    		case R.id.redSeekBar:
    			redValue = progress;
    			redText.setText(String.valueOf(redValue));
    			break;
    			
    		case R.id.greenSeekBar:
    			greenValue = progress;
    			greenText.setText(String.valueOf(greenValue));
    			break;
    			
    		case R.id.blueSeekBar:
    			blueValue = progress;
    			blueText.setText(String.valueOf(blueValue));
    			break;
    	}
    	
        updateColourView();
    }

    private void updateColourView()
	{
		int colour = Color.rgb(redValue, greenValue, blueValue);
		
		if(colourPanel != null)
			colourPanel.setBackgroundColor(colour);
	}
    
    public int getColor() 
    {
		return Color.rgb(redValue, greenValue, blueValue);
	}
    
    public void onColorChanged(int colour) 
    {
		redValue = Color.red(colour);
		greenValue = Color.green(colour);
		blueValue = Color.blue(colour);
		
		if(redBar != null)
			redBar.setProgress(redValue);
		
		if(greenBar != null)
			greenBar.setProgress(greenValue);
		
		if(blueBar != null)
			blueBar.setProgress(blueValue);
		
		updateColourView();
	}
}
