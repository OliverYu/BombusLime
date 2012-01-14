package org.bombusim.lime.service;

import org.bombusim.lime.Lime;
import org.bombusim.xmpp.XmppStream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class XmppServiceBinding {
	private XmppService xmppService;
	private ServiceConnection xsc = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			xmppService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			xmppService = ((XmppService.LocalBinder)service).getService();
		}
	};
	
	public void doBindService() { 
		Lime.getInstance().bindService(new Intent(Lime.getInstance().getApplicationContext(), XmppService.class), xsc, Context.BIND_AUTO_CREATE); 
	}

	public void doUnbindService() {
		if (xmppService != null)
			Lime.getInstance().unbindService(xsc);
	}
	
	public XmppStream getXmppStream(String rosterJid) {
		// TODO Auto-generated method stub
		XmppStream s = xmppService.getXmppStream(rosterJid);
		if (!s.loggedIn) return null;
		
		return s;
	}
}