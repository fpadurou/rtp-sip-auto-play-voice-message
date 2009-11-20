/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright (c) Ericsson AB, 2004-2007. All rights reserved.
 */

package com.ericsson.sip;

import java.io.Serializable;
import java.util.Map;
import javax.servlet.sip.SipURI;
import java.util.logging.Logger;
import java.util.logging.Level;

/* TODO Add imports */
/**
 * TODO Add comments (class description)  
 *  
 * 
 * @author ekrigro
 * @since 2005-apr-08
 *
 */
public class TimerContent implements Runnable, Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public TimerContent( SipURI key, Map target, String info ) {
      _key = key;
      _target = target;
      _info = info;
   }
   private Logger logger = Logger.getLogger( "CallSetup" );
   private SipURI _key;
   private Map _target;
   private String _info;
   
   public SipURI getKey() { return _key; }
   public void setKey( SipURI key ) { _key = key; }
   
   public Map getTarget() { return _target; }
   public void setTarget( Map target ) { _target = target; }
   
   public String getInfo() { return _info; }
   public void setInfo( String info ) { _info = info; }
   
   public void run() {
      logger.log(Level.INFO, "Timer timeout - removing key = "+_key+" info = "+_info);
      _target.remove( _key );
   }
   
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Key=").append(_key).append(" : ");
      sb.append("Target=").append(_target).append(" : ");
      sb.append("Info=").append(_info);
      return sb.toString();
   }
}
