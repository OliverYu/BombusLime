
/*
 * IqVersionReply.java
 *
 * Created on 27.02.2005, 18:31
 *
 * Copyright (c) 2005-2011, Eugene Stahov (evgs@bombus-im.org), 
 * http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * You can also redistribute and/or modify this program under the
 * terms of the Psi License, specified in the accompanied COPYING
 * file, as published by the Psi Project; either dated January 1st,
 * 2005, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


package org.bombusim.xmpp.handlers;

import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.stanza.Iq;

/**
 *
 * @author Eugene Stahov
 */
public class IqVersionReply implements XmppObjectListener {
    public IqVersionReply(){};

    public int blockArrived(XmppObject data, XmppStream stream) {
        if (!(data instanceof Iq)) return BLOCK_REJECTED;
        String type=data.getAttribute("type");
        if (type.equals("get")) {
            
            XmppObject query=data.findNamespace("query", "jabber:iq:version");
            if (query==null) return BLOCK_REJECTED;
            
            Iq reply=new Iq(data.getAttribute("from"), Iq.TYPE_RESULT, data.getAttribute("id"));
            reply.addChild(query);
            //TODO: remove hardcoded values
            query.addChild("name", "Bombus");
            query.addChild("version","0.0");
            query.addChild("os", "Green Robot");
            
            stream.send(reply);
            
            return XmppObjectListener.BLOCK_PROCESSED;
        }
        
        /*
        if (data.getAttribute("id").equals("getver")) {
            String body=null;
            if (type.equals("error")) {
                body=locale.SR.MS_NO_VERSION_AVAILABLE;
            } else if (type.equals("result")) {
                XmppObject vc=data.getChildBlock("query");
                if (vc!=null) {
                    body=dispatchVersion(vc);
                }
            } else return BLOCK_REJECTED;
            
            if (body!=null) {
                Client.Roster roster=StaticData.getInstance().roster;
                
                Msg m=new Msg(Msg.MESSAGE_TYPE_IN, "ver", locale.SR.MS_CLIENT_INFO, body);
                roster.messageStore( roster.getContact( data.getAttribute("from"), false), m);
                roster.querysign=false;
                roster.redraw();
                return JabberBlockListener.BLOCK_PROCESSED;
            }
            //
        }*/
        return BLOCK_REJECTED;

    }
    
    /*
    // constructs version request
    public static XmppObject query(String to) {
        XmppObject result=new Iq(to, Iq.TYPE_GET, "getver");
        result.addChildNs("query", "jabber:iq:version");
        return result;
    }
    
    
    private final static String TOPFIELDS []={ "name",  "version",  "os"  }; 
    

    private String dispatchVersion(XmppObject data) {
        if (!data.isJabberNameSpace("jabber:iq:version")) return "unknown version namespace";
        StringBuffer vc=new StringBuffer();
        //vc.append((char)0x01);
        for (int i=0; i<TOPFIELDS.length; i++){
            String field=data.getChildBlockText(TOPFIELDS[i]);
            if (field.length()>0) 
                vc.append(TOPFIELDS[i])
                  .append((char)0xa0)
                  .append(field)
                  .append((char)'\n');
        }
        return vc.toString();
    }
    */
}