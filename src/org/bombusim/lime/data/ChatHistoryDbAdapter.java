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

package org.bombusim.lime.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;


//TODO: create abstract class DbAdapter
public class ChatHistoryDbAdapter  {

	protected final static String DATABASE_TABLE = "messages";
	protected final static String DATABASE_NAME = "messages.db";
	protected final static int    DATABASE_VERSION = 2;
	
	public final static String KEY_ID =       "_id";

	public final static String KEY_RJID =      "rjid";
	
	public final static String KEY_TYPE =     "type";
	public final static String KEY_JID =      "jid";
	public final static String KEY_SUBJ =     "subj";
	public final static String KEY_BODY =     "body";
	public final static String KEY_TIME =     "time";
	public final static String KEY_UNREAD =   "unread";

	private HistoryDbHelper dbHelper;
	private SQLiteDatabase db;
	private String rJid;
	
	public ChatHistoryDbAdapter(Context context, String rJid) {
		dbHelper = new HistoryDbHelper(context);
		
		this.rJid = rJid;
	}
	
	public void open(boolean readOnly) {
		try {
			if (readOnly) {
				db = dbHelper.getReadableDatabase();
			} else {
				db = dbHelper.getWritableDatabase();
			}
		} catch (SQLException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}
	
	public void close() { db.close(); }
	
	public long putMessage(Message msg, long position) {
		ContentValues v = new ContentValues();
		v.put(KEY_RJID, rJid);
		v.put(KEY_TIME,     msg.timestamp);
		v.put(KEY_TYPE,     msg.type);
		v.put(KEY_JID,      msg.fromJid);
		v.put(KEY_SUBJ,     msg.subj);
		v.put(KEY_BODY,     msg.messageBody);
		v.put(KEY_UNREAD,   msg.unread? 1:0);
		
		long id=-1;
		if (position<0) {
			id = db.insert(DATABASE_TABLE, null, v);
		} else {
			id = db.update(DATABASE_TABLE, v, KEY_ID+"="+position, null);
		}
		msg.setId(id);
		return id;
	}

	public long removeMessage(long position) {
		return db.delete(DATABASE_TABLE, KEY_ID+"="+position, null);
	}
	
	//TODO: offset for paging
	public long[] getMessageIndexes(String jid, int limit) {
		//String select = (jid !=null )? KEY_JID+"='"+jid+"'" : null;
		Cursor ind = getMessageCursor(jid, limit);
		
		int count = ind.getCount(); 
		if (count == 0 || !ind.moveToFirst()) { ind.close(); return null; }
		long[] result = new long[count];
		
		int id = ind.getColumnIndex(KEY_ID);
		
		for (int i=0; i<count; i++) {
			result[i]=ind.getLong(id);
			ind.moveToNext();
		}
		
		ind.close();
		return result;
	}

	public Cursor getMessageCursor(String jid, int limit) {
		String select =  KEY_RJID + "='" + rJid + "'" 
		      +" AND " + KEY_JID  + "='"+jid+"'" ;
		
		String orderBy = KEY_TIME + " ASC";
		
		//TODO: limit now wors correctly only if order = DESC
		//TODO: limit should be by age
		String sLimit = (limit>0)? String.valueOf(limit) : null;
		
		Cursor ind = db.query(DATABASE_TABLE, null /*new String[] {KEY_ID, KEY_TIME}*/, select, null, null, null, orderBy, sLimit);
		return ind;
	}
	
	public Message getMessage(long position) {
		Cursor cursor = db.query(DATABASE_TABLE, null, KEY_ID+"="+position, null, null, null, null);
		
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) return null;
		
		Message msg=getMessageFromCursor(cursor);
		cursor.close();
		
		return msg; 
	}
	
	public static Message getMessageFromCursor(Cursor cursor) {
		int type      = cursor.getInt(cursor.getColumnIndex(KEY_TYPE));
		String jid    = cursor.getString(cursor.getColumnIndex(KEY_JID));
		String body   = cursor.getString(cursor.getColumnIndex(KEY_BODY));
		long id       = cursor.getLong(cursor.getColumnIndex(KEY_ID));
		
		Message msg = new Message(type, jid, body, id);
		
		msg.subj      = cursor.getString(cursor.getColumnIndex(KEY_SUBJ));
		msg.timestamp = cursor.getLong(cursor.getColumnIndex(KEY_TIME));
		msg.unread    = cursor.getInt(cursor.getColumnIndex(KEY_UNREAD))!=0;
		
		return msg;
	}
		
	private class HistoryDbHelper extends SQLiteOpenHelper { 
		private static final String DATABASE_CREATE = 
				"CREATE TABLE " + DATABASE_TABLE + " ("  
				        + KEY_ID  +      " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				        + KEY_RJID +      " TEXT NOT NULL, " 
				        + KEY_TYPE +     " INTEGER, " 
				        + KEY_TIME +     " INTEGER, " 
				        + KEY_JID +      " TEXT NOT NULL, " 
				        + KEY_SUBJ +     " TEXT, " 
				        + KEY_BODY +     " TEXT, " 
				        + KEY_UNREAD +   " INTEGER);";
		
		
		public HistoryDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
	
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
			onCreate(db);
		}
		
	} // AccountDbHelper

	public int countUnread(String jid) {
		String select = KEY_RJID + "='" + rJid + "'" 
			      +" AND " + KEY_JID  + "='"+jid+"'"
			      +" AND " + KEY_UNREAD + "= 1";
		
		SQLiteStatement dbUnreadCountQuery;
		
		dbUnreadCountQuery = db.compileStatement("select count(*) from " + DATABASE_TABLE + " where " + select);
		
		return (int) dbUnreadCountQuery.simpleQueryForLong();
	}

	public void markUnread(long id, boolean unread) {
		ContentValues v = new ContentValues();
		v.put(KEY_UNREAD,   unread? 1:0);
		
		id = db.update(DATABASE_TABLE, v, KEY_ID+"="+id, null);
	}
}
