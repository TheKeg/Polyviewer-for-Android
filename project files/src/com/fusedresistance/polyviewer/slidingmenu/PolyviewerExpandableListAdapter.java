package com.fusedresistance.polyviewer.slidingmenu;

import com.fusedresistance.polyviewer.pro.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PolyviewerExpandableListAdapter extends BaseExpandableListAdapter 
   implements ExpandableListAdapter
{
   public Context context;
   private LayoutInflater layoutInf;
   private String[][] data;         
   private Typeface ubuntuCondensed;
   private Typeface sourceSansPro;  
   
   public PolyviewerExpandableListAdapter(Context context, Activity activity, String[][] data)
   {
      this.context = context;
      this.data = data;
      
      layoutInf = activity.getLayoutInflater();
      
      ubuntuCondensed = Typeface.createFromAsset(context.getAssets(), "fonts/UbuntuCondensed-Regular.ttf");
      sourceSansPro   = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Regular.ttf");
   }
   
   @Override
   public String getChild(int groupPosition, int childPosition)
   {
      return data[groupPosition][childPosition];
   }

   @Override
   public long getChildId(int groupPosition, int childPosition)
   {
      return childPosition;
   }

   @Override
   public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
   {
      View view = convertView;
      String childName = getChild(groupPosition, childPosition);
      
      if(childName != null)
      {
         view = layoutInf.inflate(R.layout.menu_list_child, null);
         
         TextView textView = (TextView) view.findViewById(R.id.menu_list_child_text);
         
         textView.setText(childName);
         textView.setTypeface(sourceSansPro);
      }
      
      return view;
   }

   @Override
   public int getChildrenCount(int groupPosition)
   {
      return data[groupPosition].length;
   }

   @Override
   public String getGroup(int groupPosition)
   {
      return "group-" + groupPosition;
   }

   @Override
   public int getGroupCount()
   {
      return data.length;
   }

   @Override
   public long getGroupId(int groupPosition)
   {
      return groupPosition;
   }

   @Override
   public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
   {
      View view = convertView;
      String groupName = null;
      int img_id = 0;
      
      switch(groupPosition)
      {
         case 0:
            groupName = "Load Model"; 
            img_id    = R.drawable.folder;
            
            break;
            
         case 1:
            groupName = "Camera Presets"; 
            img_id    = R.drawable.camera;
            
            break;
            
         case 2:
            groupName = "Light Presets"; 
            img_id    = R.drawable.lights;
            
            break;
      }
      
      if(groupName != null)
      {
         view = layoutInf.inflate(R.layout.menu_list_parent, null);
         
         TextView textView = (TextView)view.findViewById(R.id.menu_list_parent_text);
         ImageView img     = (ImageView)view.findViewById(R.id.menu_list_parent_icon);
         
         textView.setText(groupName);
         textView.setTypeface(ubuntuCondensed);
         
         img.setImageResource(img_id);
      }
      
      return view;
   }

   @Override
   public boolean hasStableIds()
   {
      return true;
   }

   @Override
   public boolean isChildSelectable(int groupPosition, int childPosition)
   {
      // TODO Auto-generated method stub
      return true;
   }
   
}
