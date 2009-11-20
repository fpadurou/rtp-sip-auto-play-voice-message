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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.*;
import javax.servlet.sip.*;
import javax.persistence.*;
import javax.servlet.http.*;

import java.util.logging.*;

import com.ericsson.sip.Registration;

import com.sqlmanager.*;

/**
 * @author lmcpepe
 * @created 3-Aug-2004
 * @modified BlueSorcerer 2009
 */
@PersistenceContext(name = "persistence/LogicalName", unitName = "EricssonSipPU")
public class RegistrarServlet extends SipServlet
{
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private Logger                          logger              = Logger.getLogger("CallSetup");
    private static final long               serialVersionUID = 3977861786890484016L;
    public static final String              REGISTRATION_MAP = "REGISTRATION_MAP";
    private static final long 				SESSION_TIMER = 60;
    
    ServletContext ctx = null;
    SipFactory sf = null;
    
    protected void doRegister(SipServletRequest request) throws ServletException, IOException 
	{
        SipServletResponse response = request.createResponse(200);
		
        try 
		{
            SipURI to = cleanURI((SipURI) request.getTo().getURI());
            ListIterator<Address> li = request.getAddressHeaders("Contact");

            while (li.hasNext())
			{
                Address na = li.next();
                SipURI contact = (SipURI) na.getURI();
				
				String strContact = contact.toString();
				int startIndex = strContact.indexOf(":") + 1;
				int endIndex = strContact.indexOf("@");
				String strUserName = strContact.substring(startIndex, endIndex);

//A				logger.log(Level.INFO, "RegistrarServlet: STEP 1: strUserName = " + strUserName);
				
				// --- if username is not valid, send 484 - Address Incomplete
				if(strUserName == null || strUserName.length() == 0)
				{
					logger.log(Level.INFO, "Send 484 response.");
					response.setStatus(484);
					continue;
				}
				
				// --- look in database to see if the user is online
				int nUserId = (int)Integer.parseInt(strUserName);
				
                String expiresString = na.getParameter("expires");
                
                if (expiresString == null)
				{ 
					// Check the Expires header
                    expiresString = request.getHeader("Expires");
                }
                if (expiresString == null) 
				{
                    expiresString = "" + SESSION_TIMER;
                }
                
                long expires = Integer.parseInt(expiresString);
                
				// --- unregister request
                if (expires == 0) 
				{
					logger.log(Level.INFO, "RegistrarServlet: Logout request " + strContact);
					
					// --- remove sip address from database
					SqlManager.setSipAddress(nUserId, null);
                } 
				else 
				{
					expires = SESSION_TIMER;
					response.setExpires((int)expires);
					
					// --- if he is online, update sip address in database
					if(SqlManager.isOnline(nUserId))
					{
						// --- Update sip adress in database
						SqlManager.setSipAddress(nUserId, strContact);
						logger.log(Level.INFO, "RegistrarServlet: Registration was successfully created for user: " + strContact);
					}
					else
					{
						logger.log(Level.INFO, "RegistrarServlet: User not online. Send 480 response.");
						response.setStatus(480);
						continue;
					}

					response.setHeader("Contact",na.toString());
                }
            }
            response.send();
        } 
		catch(Exception e) 
		{
            logger.log(Level.INFO, "RegistrarServlet: Sent 500 response. ", e);
            response.setStatus(500);
            response.send();
        }
		
        SipApplicationSession appsess = request.getApplicationSession(false);
        if (appsess != null) 
		{
            appsess.invalidate();
        }
    }
    
    protected void doInvite(SipServletRequest request) throws ServletException, IOException 
	{
        logger.log(Level.INFO, "RegistrarServlet: doInvite()");
        doProxy(request);
    }
    
   /*
    * (non-Javadoc)
    *
    * @see javax.servlet.sip.SipServlet#doMessage(javax.servlet.sip.SipServletRequest)
    */
    protected void doMessage(SipServletRequest request) throws ServletException, IOException {
        logger.log(Level.INFO, "RegistrarServlet: doMessage()");
        doProxy(request);
        
        SipApplicationSession appsess = request.getApplicationSession(false);
        if (appsess != null) {
            appsess.invalidate();
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ctx = config.getServletContext();
        sf = (SipFactory) ctx.getAttribute(SipServlet.SIP_FACTORY);
		logger.log(Level.INFO, "Started RegistrarServlet");
    }
    
   /*
    * (non-Javadoc)
    *
    * @see javax.servlet.sip.SipServlet#doOptions(javax.servlet.sip.SipServletRequest)
    */
    protected void doOptions(SipServletRequest request) throws ServletException, IOException {
        int maxFw = request.getMaxForwards();
        if (maxFw == 0) {
            logger.log(Level.INFO, "doOptions - UAS");
            SipServletResponse response = request.createResponse(200);
            response.send();
        } else { // Proxy
            logger.log(Level.INFO,"doOptions - Proxy");
            doProxy(request);
        }
        SipApplicationSession appsess = request.getApplicationSession(false);
        if (appsess != null) {
            appsess.invalidate();
            logger.log(Level.FINE, "appsession.invalidate()");
        }
    }
    
   /*
    * (non-Javadoc)
    *
    * @see javax.servlet.sip.SipServlet#doResponse(javax.servlet.sip.SipServletResponse)
    */
    protected void doResponse(SipServletResponse resp) throws ServletException, IOException {
        logger.log(Level.INFO, "RegistrarServlet: Servlet got response = " + resp);
        // resp.getSession().invalidate();
        SipApplicationSession appsess = resp.getApplicationSession(false);
        if (appsess != null) {
            appsess.invalidate();
        }
    }
    
    void doProxy(SipServletRequest request) throws ServletException, IOException {
        SipURI reqURI = cleanURI((SipURI) request.getRequestURI());
        Map reg = (Map) ctx.getAttribute(REGISTRATION_MAP);
        boolean foundProxy = false;
        if(reg != null){
            Iterator<SipURI> i = reg.keySet().iterator();
            while( i.hasNext() ) {
                SipURI target = i.next();
                SipURI to = (SipURI) reg.get(target);
                if( to.equals(reqURI) ) {
                    Proxy proxy = request.getProxy();
                    proxy.setRecordRoute( false );
                    logger.log(Level.INFO,"RegistrarServlet: doProxy: Proxy reqUri = " + reqURI + " to " + target);
                    if (target != null) proxy.proxyTo(target);
                    foundProxy = true;
                }
            }
        }
        if(!foundProxy) {
            logger.log(Level.INFO, "RegistrarServlet: doProxy: Sending 404");
            SipServletResponse response = request.createResponse(404);
            response.send();
        }
    }
    
    SipURI cleanURI(SipURI original) 
	{
        SipURI copy = (SipURI) original.clone();
        Iterator headers = copy.getHeaderNames();
        if (headers.hasNext()) {
            headers.next();
            headers.remove();
        }
        Iterator parameters = copy.getParameterNames();
        if (parameters.hasNext()) 
		{
            String param = (String) parameters.next();
            copy.removeParameter(param);
        }
        return copy;
    }
    
 	private List<SipSession> getSipSessions(SipApplicationSession sas) 
	{
		if(sas == null)
			return null;
		
        List<SipSession> sipSessions = new ArrayList<SipSession>();
        Iterator<SipSession> iter =
                (Iterator<SipSession>) sas.getSessions("SIP");
        while (iter.hasNext()) 
		{
            sipSessions.add(iter.next());
        }
        return sipSessions;
    }
    
}
