package com.fusedresistance.polyviewer.pro;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.fusedresistance.polyviewer.settings.PolyviewerOptions;
import com.fusedresistance.polyviewer.settings.Preferences;
import com.fusedresistance.polyviewer.utility.FileLoader;
import com.fusedresistance.polyviewer.utility.PathDatabase;

public class HelpActivity extends SherlockActivity implements OnClickListener, android.content.DialogInterface.OnClickListener
{
	private int numViews = 0;
	private View menuView;
	private String[] availableModels = null;
	private String[] modelPaths = null;
	private String[] helpPages = null;
	private int[] helpPageIDs = null;
	private String dialogTitle = "";
	
	//@Override
	public void onCreate(Bundle savedInstance)
	{
		setTitle("Help");
		setTheme(Preferences.THEME);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		
		ActionBar actionBar = getSupportActionBar();

		if(actionBar != null)
		{
			actionBar.setIcon(R.drawable.menu_icon);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		super.onCreate(savedInstance);
		
//		context = this;
		
		LayoutInflater inflator = getLayoutInflater();
		View helpView = inflator.inflate(R.layout.gallery_view, null);

		try
		{	
			PathDatabase pathDB = new PathDatabase(this, Preferences.PATH_DATABASE);
        	pathDB.open();
        	
        	String[] polyviewerDirs = pathDB.getList();
        	
        	modelPaths = FileLoader.retrieveMeshDirectories(polyviewerDirs);
    		availableModels = FileLoader.parseString(modelPaths);
			
			if(availableModels != null)
				numViews = availableModels.length;
		}
		catch(Exception e)
		{
			if(e.getLocalizedMessage() != null)
				Log.d("MODEL ERROR", e.getLocalizedMessage());
        	else if(e.getMessage() != null)
        		Log.e("MODEL ERROR", e.getMessage());
        	else
        		Log.e("MODEL ERROR", e.toString());
		}
		
		PolyviewerPagerAdapter pAdapter = new PolyviewerPagerAdapter();
		ViewPager pager = (ViewPager)helpView.findViewById(R.id.pager);
		pager.setAdapter(pAdapter);

		helpPages = getResources().getStringArray(R.array.helpSections);
		
		if(helpPages != null)
			numViews = helpPages.length;
		
		helpPageIDs = new int[numViews];
		
		if(helpPageIDs.length >= 6)
		{
			helpPageIDs[0] = R.layout.help_features;
			helpPageIDs[1] = R.layout.help_interface;
			helpPageIDs[2] = R.layout.help_drives;
			helpPageIDs[3] = R.layout.help_folders;
			helpPageIDs[4] = R.layout.help_naming;
			helpPageIDs[5] = R.layout.help_multi;
			helpPageIDs[6] = R.layout.help_misc;
		}
		
		initializeMenu();
		
		LayoutParams menuLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		setContentView(helpView);
		addContentView(menuView, menuLayoutParams);
	}

	private void initializeMenu()
	{
	   LayoutInflater inflator = getLayoutInflater();
      menuView = inflator.inflate(R.layout.slidemenu, null);
      menuView.setVisibility(View.GONE);
        
      View view = menuView.findViewById(R.id.view_model_linear);
        
      if(view != null)
        	view.setOnClickListener(this);
        
      view = menuView.findViewById(R.id.gallery_linear);
        
      if(view != null)
        	view.setOnClickListener(this);

      view = menuView.findViewById(R.id.help_linear);
        
      if(view != null)
        	view.setOnClickListener(this);
        
      view = menuView.findViewById(R.id.options_linear);
      view.setOnClickListener(this);
	}

	//@Override
	public void onClick(View v)
	{
		int viewID = v.getId();
		AlertDialog.Builder alertBuilder;
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
			
		switch(viewID)
		{
			case R.id.view_model_linear:
				if(menuView != null)
				{
					menuView.setVisibility(View.GONE);
					menuView.setAnimation(anim);
				}
			case R.id.view_model:
				
				try
				{
//					if(availableModels == null)
//						availableModels = FileLoader.availableModels(this);
				
				
					if(availableModels == null)
					{
						Toast toast = Toast.makeText(this, "Model List is NULL", Toast.LENGTH_SHORT);
						toast.show();
						
						return;
					}
					
					alertBuilder = new AlertDialog.Builder(this);
			        alertBuilder.setTitle("Select Model");
			        alertBuilder.setItems(availableModels, this);
			      
			        AlertDialog modelAlert = alertBuilder.create();
					
					if(modelAlert != null)
						modelAlert.show();
					
					dialogTitle = "viewModel";
				}
				catch(Exception e)
				{
					if(e.getLocalizedMessage() != null)
					{
						Log.d("GALLERY MENU ERROR", e.getLocalizedMessage());
						
						Toast toast = Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT);
						toast.show();
					}
					else
					{
						Toast toast = Toast.makeText(this, "Null Exception Error", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
		        
				break;

			case R.id.options_linear:
				if(menuView != null)
				{
					menuView.setVisibility(View.GONE);
					menuView.setAnimation(anim);
				}
			case R.id.options_button:
				
				Intent optionsIntent = new Intent(this, PolyviewerOptions.class);
				
				startActivity(optionsIntent);
				
				break;
			
			case R.id.help_linear:
				if(menuView != null)
				{
					menuView.setVisibility(View.GONE);
					menuView.setAnimation(anim);
				}
			case R.id.help_activity:
				
//				LayoutInflater inflater = getLayoutInflater();
//				View helpView = inflater.inflate(R.layout.help_layout, (ViewGroup)getCurrentFocus());
//				
//				alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, PolyviewerActivity.THEME));
//				
//				alertBuilder.setTitle("Help");
//				alertBuilder.setView(helpView);
//				alertBuilder.setPositiveButton("OK", null);
//				
//				AlertDialog helpDialog = alertBuilder.create();
//				
//				helpDialog.show();

				break;
				
//			case R.id.cloud_drive_linear:
//				if(menuView != null)
//				{
//					menuView.setVisibility(View.GONE);
//					menuView.setAnimation(anim);
//				}
//				break;
		}
	}

	//@Override
	public void onClick(DialogInterface dialog, int buttonID)
	{
		if(dialogTitle.equals("viewModel"))
		{
			final ActivityManager actMan = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
			final ConfigurationInfo configInfo = actMan.getDeviceConfigurationInfo();
			final boolean supportES2 = configInfo.reqGlEsVersion >= 0x20000;
			
			if(supportES2)
			{
				Intent newIntent = new Intent(this, PolyviewerActivity.class);
				newIntent.putExtra(PolyviewerActivity.MODEL, modelPaths[buttonID]);
				
				startActivity(newIntent);
			}
			
			dialogTitle = "";
		}		
	}
	
	private class PolyviewerPagerAdapter extends PagerAdapter
	{
		//@Override
        public int getCount() 
        {
    		return numViews;
        }
        
        //@Override
        public CharSequence getPageTitle(int position)
        {
        	if(helpPages == null)
        		return null;
        	if(position >= helpPages.length)
        		return null;
        	
        	return helpPages[position];
        }

    /**
     * Create the page for the given position.  The adapter is responsible
     * for adding the view to the container given here, although it only
     * must ensure this is done by the time it returns from.
     *
     * @param collection The containing View in which the page will be shown.
     * @param position The page position to be instantiated.
     * @return Returns an Object representing the new page.  This does not
     * need to be a View, but can be some other container of the page.
     */
   //@Override
   public Object instantiateItem(View collection, int position)
   {
      if(position >= helpPageIDs.length)
         return null;

      LayoutInflater inflater = getLayoutInflater();
      View view = inflater.inflate(helpPageIDs[position], null);

      ((ViewPager)collection).addView(view, 0);

      return view;
   }

	    /**
	     * Remove a page for the given position.  The adapter is responsible
	     * for removing the view from its container, although it only must ensure
	     * this is done by the time it returns from.
	     *
	     * @param collection The containing View from which the page will be removed.
	     * @param position The page position to be removed.
	     * @param view The same object that was returned by
	     * {@link #instantiateItem(View, int)}.
	     */
        //@Override
        public void destroyItem(View collection, int position, Object view) 
        {
        	((ViewPager)collection).removeView((View)view);
        }

        
        
        //@Override
        public boolean isViewFromObject(View view, Object object) 
        {
        	return view == ((View)object);
        }

        
	    /**
	     * Called when the a change in the shown pages has been completed.  At this
	     * point you must ensure that all of the pages have actually been added or
	     * removed from the container as appropriate.
	     */
        //@Override
        public void finishUpdate(View arg0) {}
        

        //@Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {}

        //@Override
        public Parcelable saveState() {
                return null;
        }

        //@Override
        public void startUpdate(View arg0) {}
	}
}
