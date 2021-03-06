/*
 * Copyright (c) 2005-2011, Eugene Stahov (evgs@bombus-im.org), 
 * http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.bombusim.xmpp.stanza;

import java.util.ArrayList;

import org.bombusim.xml.Attributes;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppError;

import android.text.format.Time;


/**
 * Title:        Message.java
 * Description:  The class representing a Jabber message object
 */

public final class XmppMessage extends XmppObject
{
  /**
   * Constructor. Prepares the message destination and body
   *
   * @param to The destination of the message
   * @param message The message text
   */

  public XmppMessage( String to, String message , String subject, boolean groupchat)
  {
    super();

    setAttribute( "to", to );
    if( message != null )
      setBodyText( message );
    if (subject!=null) 
        setSubject(subject);
    setTypeAttribute((groupchat)?"groupchat":"chat");
  }

  /**
   * Constructor. Prepares the message destination
   *
   * @param to The destination of the message
   */

  public XmppMessage( String to ) {
	  this(to, null, null, false);
  }

  /**
   * Constructor for incomming messages
   *
   * @param _parent The parent of this datablock
   * @param _attributes The list of element attributes
   */

  public XmppMessage( XmppObject parent, Attributes attributes ) {
    super( parent, attributes );
  }

  /**
   * Method to set the body text. Creates a block with body as it's tag name
   * and inserts the text into it.
   *
   * @param bodyText The string to go in the message body
   */

  public void setBodyText( String text )
  {
    addChild( "body", text );
  }

  /**
   * Method to set the body text written in HTML. Creates a block with html as
   * it's tag name in the xhtml name space and inserts the html into it.
   *
   * @param html The html to go in the message body
   */

  /*
  public void setHTMLBodyText( String html )
  {
    JabberDataBlock body = new JabberDataBlock( "html", null, null );
    body.setNameSpace( "http://www.w3.org/1999/xhtml" );
    body.addText( html );
    addChild( body );
  }
   */

  /**
   * Method to set the message thread. Creates a block with thread as it's tag
   * name and inserts the thread name into it.
   *
   * @param threadName The string to go in the thread block
   */

  /*public void setThread( String text )
  {
    JabberDataBlock thread = new JabberDataBlock( "thread", null, null );
    thread.addText( text );
    addChild( thread );
  }*/

  /**
   * Method to set the subject text. Creates a subject block and inserts the text into it.
   *
   * @param text The string to go in the message subject
   */

  public void setSubject( String text ) { addChild( "subject", text ); }


  /**
   * Method to get the message subject
   *
   * @return A string representing the message subject
   */

  public String getSubject() {  return getChildBlockText( "subject" );  }

  /**
   * Method to get the message body
   *
   * @return The message body as a string
   */

  public String getBody() { 
      String body=getChildBlockText( "body" ); 
      
      XmppObject error=getChildBlock("error");
      if (error==null) return body;
      return body+"Error\n"+XmppError.decodeStanzaError(error).toString();
  }

  public long getTimeStamp() {

	  Time t = new Time();

      try {
    	  //ISO8601/RFC3339 timestamp
    	  t.parse3339( findNamespace("delay", "urn:xmpp:delay").getAttribute("stamp") );
          return t.toMillis(false);
          
      } catch (Exception e) { }

      try {
		  //legacy timestamp
		  t.parse( findNamespace("x", "jabber:x:delay").getAttribute("stamp") );
		  return t.toMillis(false);
		  
      } catch (Exception e) { }
      
      return System.currentTimeMillis();
  }

  /**
   * Get the tag start marker
   *
   * @return The block start tag
   */

  public String getTagName()
  {
    return "message";
  }

  /**
     * Method to get the message from field
     * @return <B>from</B> field as a string
     */
    public String getXFrom() {
	//try {
	//    // jep-0146
	//    JabberDataBlock fwd=findNamespace("jabber:x:forward"); // DEPRECATED
	//    JabberDataBlock from=fwd.getChildBlock("from");
	//    return from.getAttribute("jid");
	//} catch (Exception ex) { /* normal case if not forwarded message */ };
	
	try {
	    // jep-0033 extended stanza addressing from psi
	    ArrayList<XmppObject> addresses=getChildBlock("addresses").getChildBlocks();
	    
	    for (int index=0; index<addresses.size(); index++) { 
	    	XmppObject adr=addresses.get(index);
	    	if (adr.getTypeAttribute().equals("ofrom")) return adr.getAttribute("jid");
	    }
	} catch (Exception e) { /* normal case if not forwarded message */ };
        return getAttribute("from");
    }
    
    public String getFrom() {
        return getAttribute("from");
    }

}
