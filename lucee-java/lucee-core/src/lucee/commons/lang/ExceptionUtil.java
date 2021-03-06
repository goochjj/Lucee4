/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.commons.lang;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;

import lucee.runtime.exp.NativeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public final class ExceptionUtil {
	
	public static String getStacktrace(Throwable t, boolean addMessage) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		String st = sw.toString();
		String msg=t.getMessage();
		if(addMessage && !StringUtil.isEmpty(msg) && !st.startsWith(msg.trim()))
			st=msg+"\n"+st;
		return st;
		
	}
	

	public static String getMessage(Throwable t) {
		String msg=t.getMessage();
		if(StringUtil.isEmpty(msg,true)) msg=t.getClass().getName();
		
		StringBuilder sb=new StringBuilder(msg);
		
		if(t instanceof PageException){
			PageException pe=(PageException)t;
			String detail = pe.getDetail();
			if(!StringUtil.isEmpty(detail,true)) {
				sb.append('\n');
				sb.append(detail);
			}
		}
		return sb.toString();
	}

	public static PageException addHint(PageExceptionImpl pe,String hint) {
		pe.setAdditional(KeyConstants._Hint, hint);
		return pe;
	}
	
	
	/**
	 * creates a message for key not found with soundex check for similar key
	 * @param keys
	 * @param keyLabel
	 * @return
	 */
	public static String similarKeyMessage(Collection.Key[] _keys,String keySearched, String keyLabel, String keyLabels, boolean listAll) {
		boolean empty=_keys.length==0;
		if(listAll && (_keys.length>50 || empty)) {
			listAll=false;
		}
		
		String list=null;
		if(listAll) {
			Arrays.sort(_keys);
			list=ListUtil.arrayToList(_keys, ",");
		}
		String keySearchedSoundex=StringUtil.soundex(keySearched);
		
		for(int i=0;i<_keys.length;i++){
			if(StringUtil.soundex(_keys[i].getString()).equals(keySearchedSoundex)) {
				String appendix;
				if(listAll) appendix=". Here is a complete list of all available "+keyLabels+": ["+list+"].";
				else if(empty) appendix=". The structure is empty";
				else appendix=".";
				
				return "The "+keyLabel+" ["+keySearched+"] does not exist, but there is a similar "+keyLabel+" with name ["+_keys[i].getString()+"] available"+appendix;
			}
		}
		String appendix;
		if(listAll) appendix=", only the following "+keyLabels+" are available: ["+list+"].";
		else if(empty) appendix=", the structure is empty";
		else appendix=".";
		return "The "+keyLabel+" ["+keySearched+"] does not exist"+appendix;
	}
	

	public static String similarKeyMessage(Collection coll,String keySearched, String keyLabel, String keyLabels, boolean listAll) {
		return similarKeyMessage(CollectionUtil.keys(coll), keySearched, keyLabel, keyLabels,listAll);
	}

	public static IOException toIOException(Throwable t) {
		if(t instanceof IOException) return (IOException) t;
		if(t instanceof InvocationTargetException) return toIOException(((InvocationTargetException) t).getCause());
		if(t instanceof NativeException) return toIOException(((NativeException)t).getCause());
		
		IOException ioe = new IOException(t.getClass().getName()+":"+t.getMessage());
		ioe.setStackTrace(t.getStackTrace());
		return ioe;
	}


	public static String createSoundexDetail(String name, Iterator<String> it, String keyName) {
		StringBuilder sb=new StringBuilder();
		String k ,sname=StringUtil.soundex(name);
		while(it.hasNext()){
			k = it.next();
			if(StringUtil.soundex(k).equals(sname))
				return "did you mean ["+k+"]";
			if(sb.length()!=0)sb.append(',');
			sb.append(k);
		}
		return "available "+keyName+" are ["+sb+"]";
	}

	public static RuntimeException toRuntimeException(Throwable t) {
		return new RuntimeException(t);
	}
}
