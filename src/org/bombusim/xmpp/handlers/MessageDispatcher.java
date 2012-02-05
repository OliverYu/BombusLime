package org.bombusim.xmpp.handlers;

import java.io.IOException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.ChatFactory;
import org.bombusim.lime.data.Message;
import org.bombusim.lime.data.Roster;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.XmppMessage;

public class MessageDispatcher implements XmppObjectListener{

	public static final String URN_XMPP_RECEIPTS = "urn:xmpp:receipts";

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		if (!(data instanceof XmppMessage)) return BLOCK_REJECTED;
		
		XmppMessage m = (XmppMessage) data;
		XmppJid from = new XmppJid( m.getFrom() );

		String body = m.getBody();

		String id = m.getAttribute("id");

		if (m.findNamespace("received", URN_XMPP_RECEIPTS) != null) {
			stream.sendBroadcast(Chat.DELIVERED, id);
			return BLOCK_PROCESSED;
		}
		
		//TODO: composing events
		if (body.length() == 0) return BLOCK_REJECTED;
		
		Message msg = new Message(Message.TYPE_MESSAGE_IN, from.getBareJid(), body);
		msg.subj = m.getSubject();
		//TODO: timestamp
		
		//TODO: resource magic
		try {
			
			Chat c = Lime.getInstance().getChatFactory().getChat(from.getBareJid(), stream.jid); 
			c.addMessage(msg);
			Lime.getInstance().notificationMgr().showChatNotification(c.getVisavis(), body);
			
			if (m.findNamespace("request", URN_XMPP_RECEIPTS)!=null) {
				XmppMessage confirmReceived = new XmppMessage(m.getFrom());
				confirmReceived.setAttribute("id", id);
				confirmReceived.addChildNs("received", URN_XMPP_RECEIPTS);
				
				stream.send(confirmReceived);
			}
			
			stream.sendBroadcast(Chat.UPDATE_CHAT);

		} catch (Exception e) {
			e.printStackTrace(); //Will handle only NPE
		}
		
		
		
		return BLOCK_PROCESSED;
	}

	@Override
	public String capsXmlns() { return URN_XMPP_RECEIPTS; }

}
