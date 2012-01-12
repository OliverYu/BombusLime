package org.bombusim.lime.logger;

import java.util.ArrayList;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class LoggerActivity extends Activity {
    ListView logListView;
	
    CheckBox loggerEnabled;

	int logSize; //caching getLogRecords().size() to provide atomic updating

	// Time formatter
	private Time tf=new Time(Time.getCurrentTimezone());
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.log);
        
        logListView = (ListView) findViewById(R.id.loggerListView);
        logListView.setAdapter(new LogListAdapter(this));
        
        logListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				
				ListView l = (ListView) arg0;
				((LogListAdapter)l.getAdapter()).toggle(position);
			}
		});
        
        loggerEnabled = (CheckBox) findViewById(R.id.loggerEnable);
        
        loggerEnabled.setChecked(Lime.getInstance().localXmlEnabled);
        loggerEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Lime.getInstance().localXmlEnabled = isChecked;
			}
		});
        
        ((Button) findViewById(R.id.loggerClear)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Lime.getInstance().getLog().clear();
				updateLogSize();
			}
		});
        
        updateLogSize();
    }
        
    
    
    private class LogListAdapter extends BaseAdapter {
    	
        public LogListAdapter(Context context)
        {
            mContext = context;
        }

        
        private ArrayList<LoggerEvent> getLogRecords() { return Lime.getInstance().getLog().getLogRecords(); }
        
        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         * 
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return logSize;
        }

        /**
         * Since the data comes from an array, just returning
         * the index is sufficent to get at the data. If we
         * were using a more complex data structure, we
         * would return whatever object represents one 
         * row in the list.
         * 
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a SpeechView to hold each row.
         * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            LogEventView sv;
            LoggerEvent p=getLogRecords().get(position);
            
            if (convertView == null) {
                sv = new LogEventView(mContext);
            } else {
                sv = (LogEventView)convertView;
            }
                sv.setColor(p.getEventColor());
                sv.setText(p.timestamp, p.title, p.message);
                sv.setExpanded(p.expanded);
            
            return sv;
        }

        public void toggle(int position) {
            LoggerEvent p=getLogRecords().get(position);
            p.expanded = !p.expanded; 
            notifyDataSetChanged();
        }
        
        /**
         * Remember our context so we can use it when constructing views.
         */
        private Context mContext;
        
    }
    /**
     * We will use a LogEventView to display each LoggerEvent. It's just a LinearLayout
     * with two text fields.
     *
     */
    private class LogEventView extends LinearLayout {
        public LogEventView(Context context) {
            super(context);
            
            this.setOrientation(VERTICAL);
            
            // Here we build the child views in code. They could also have
            // been specified in an XML file.
            
            mDetails = new TextView(context);
            mTitle = new TextView(context);
            mTime = new TextView(context);
            
            mTime.setPadding(0, 0, 6, 0);
            
            LinearLayout s1 = new LinearLayout(context);
            s1.setOrientation(HORIZONTAL);
            s1.addView(mTime, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            s1.addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            
            addView(s1);
            addView(mDetails, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            setExpanded(false);
        }
        
        
        public void setText(long time, String title, String message) {
        	tf.set(time);
        	
        	mTime.setText(tf.format("%H:%M"));
        	
            mTitle.setText(title);
            mDetails.setText(message);
		}

        public void setColor(int color) {
        	mTitle.setTextColor(color);
        	mDetails.setTextColor(color);
        }
        /**
         * Convenience method to expand or hide the item
         */
        public void setExpanded(boolean expanded) {
            mDetails.setVisibility(expanded ? VISIBLE : GONE);
        }
        
        private TextView mTime;
        private TextView mDetails;
        private TextView mTitle;
    }
    
	private class LoggerBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			updateLogSize();
		}
		
	}
	
	private void updateLogSize() {
		logListView.setVisibility(View.GONE);
		logSize = Lime.getInstance().getLog().getLogRecords().size();
		((BaseAdapter)logListView.getAdapter()).notifyDataSetChanged();
		logListView.invalidate();
		logListView.setVisibility(View.VISIBLE);
	}
	
	LoggerBroadcastReceiver br;
	@Override
	protected void onResume() {
		super.onResume();
		br = new LoggerBroadcastReceiver();
		registerReceiver(br, new IntentFilter(LoggerData.UPDATE_LOG));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(br);
	}    
}
