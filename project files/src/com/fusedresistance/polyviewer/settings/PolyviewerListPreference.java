package com.fusedresistance.polyviewer.settings;

import com.fusedresistance.polyviewer.pro.R;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class PolyviewerListPreference extends ListPreference 
{
   private Context parentContext;
   private View prefView;
   
   public PolyviewerListPreference(Context context)
   {
      super(context);

      parentContext = context;
   }
   
   public PolyviewerListPreference(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      parentContext = context;
   }

   @Override
   protected void onBindView(View view)
   {
      super.onBindView(view);
      
      Typeface ubuntuCondensed = Typeface.createFromAsset(parentContext.getAssets(), "fonts/UbuntuCondensed-Regular.ttf");
      Typeface sourceSansPro   = Typeface.createFromAsset(parentContext.getAssets(), "fonts/SourceSansPro-Regular.ttf");
      TextView titleView       = (TextView) view.findViewById(android.R.id.title);
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
   
   public void setTheme(int styleID)
   {
      if(prefView != null)
         return;
      
      TextView titleView       = (TextView)prefView.findViewById(android.R.id.title);
      TextView summaryView     = (TextView)prefView.findViewById(android.R.id.summary);
      
      if(titleView != null)
         titleView.setTextAppearance(parentContext, styleID);
      
      if(summaryView != null)
         summaryView.setTextAppearance(parentContext, styleID);
   }
}
