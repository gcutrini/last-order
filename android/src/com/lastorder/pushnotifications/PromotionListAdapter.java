package com.lastorder.pushnotifications;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PromotionListAdapter extends BaseAdapter {
	
	private ArrayList<Promotion> promotions;
	private LayoutInflater inflater;
	private Context context;
	public PromotionListAdapter(ArrayList<Promotion> promos, Context c) {
		promotions = promos;
		context = c;
		inflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return promotions.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return promotions.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.promotion_list, null);
			holder.venue = (TextView)convertView.findViewById(R.id.venue);
			holder.name = (TextView)convertView.findViewById(R.id.name);
			holder.description = (TextView)convertView.findViewById(R.id.description);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.venue.setText(promotions.get(position).venue);
		holder.name.setText(promotions.get(position).name);
		holder.description.setText(promotions.get(position).description);
		
		return convertView;
	}
	
	private class ViewHolder {
		TextView venue;
		TextView name;
		TextView description;
		
	}

}
