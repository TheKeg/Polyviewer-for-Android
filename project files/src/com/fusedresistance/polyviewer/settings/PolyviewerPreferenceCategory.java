package com.fusedresistance.polyviewer.settings;

import com.fusedresistance.polyviewer.pro.R;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class PolyviewerPreferenceCategory extends PreferenceCategory
{
   private Context parentContext;
   private View prefView;
   
   public PolyviewerPreferenceCategory(Context context)
   {
      super(context);

      parentContext = context;
   }
   
   public PolyviewerPreferenceCategory(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      parentContext = context;
   }
   
   @Override
   protected void onBindView(View view)
   {
      super.onBindView(view);
      
      Typeface ubuntuCondensed = Typeface.createFromAsset(parentContext.getAssets(), "fonts/UbuntuCondensed-Regular.ttf");
      TextView titleView       = (TextView) view.findViewById(android.R.id.title);
      
      prefView = view;
      prefView.setBackgroundColor(Preferences.DARK_COLOUR);
      
      if(titleView != null)
      {
         titleView.setTypeface(ubuntuCondensed);
         titleView.setTextAppearance(parentContext, R.style.PolyPreference);
      }   
   }
   
   public void setTheme(int styleID)
   {
      if(prefView != null)
         return;
      
      TextView titleView = (TextView)prefView.findViewById(android.R.id.title);
      
      if(titleView != null)
         titleView.setTextAppearance(parentContext, styleID);
   }
}
