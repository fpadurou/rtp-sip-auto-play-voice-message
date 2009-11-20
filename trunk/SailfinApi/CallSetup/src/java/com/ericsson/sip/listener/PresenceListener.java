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

package com.ericsson.sip.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.TimerListener;

public class PresenceListener implements TimerListener 
{
	private Logger logger = Logger.getLogger("CallSetup");
	
	public void timeout(ServletTimer timer) 
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "PresenceListener: timeout " + timer.toString());
	}

}
