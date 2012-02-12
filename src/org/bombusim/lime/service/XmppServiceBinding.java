package org.bombusim.lime.service;

import org.bombusim.lime.Lime;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.stanza.XmppMessage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class XmppServiceBinding {
	private Context context;
	
	private XmppServiceBinding(){};
	public XmppServiceBinding(Context context) {
		this();
		this.context = context;
	}
	
	private XmppService xmppService;
	private ServiceConnection xsc = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			xmppService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			xmppService = ((XmppService.LocalBinder)service).getService();
			if (bindlistener !=null) bindlistener.onBindService(xmppService);
		}
	};
	
	public interface BindListener {
		abstract void onBindService(XmppService service);
	}
	
	private BindListener bindlistener;
	
	public void setBindListener(BindListener bl) {
		this.bindlistener = bl;
	}
	
	public void doBindService() { 
		Lime.getInstance().bindService(new Intent(context, XmppService.class), xsc, Context.BIND_AUTO_CREATE); 
	}

	public void doUnbindService() {
		if (xmppService != null)
			Lime.getInstance().unbindService(xsc);
	}
	
	public XmppStream getXmppStream(String rosterJid) {
		XmppStream s = xmppService.getXmppStream(rosterJid);
		if (s == null) return null;
		if (!s.loggedIn) return null;
		
		return s;
	}
	
	public boolean isLoggedIn(String rosterJid) {
		return getXmppStream(rosterJid)!=null;
	}
	
	public void doDisconnect() {
		xmppService.disconnectAll();
	}
	
	/**
	 * Sending XMPP stanza
	 * @param streamJid - JID identifies stream so be used  
	 * @param stanza stanza to be sent
	 * @return true if success, false otherwise
	 */
	public boolean sendStanza(String streamJid, XmppObject stanza) {
		try {
			return getXmppStream(streamJid).send(stanza);
		} catch (Exception e) {	}
		
		return false;
		
	}
	
	/**
	 * USE WITH CAUTION!!!
	 * @return XmppService object
	 */
	public XmppService getXmppService() {
		return xmppService;
	}
}
