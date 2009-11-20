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

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.sqlmanager.*;
import javax.servlet.sip.*;
import java.util.logging.*;
import javax.naming.*;

/**
 *
 * @author BlueSorcerer
 * @version
 */
 
public class EndCallServlet extends HttpServlet 
{
	private Logger logger = Logger.getLogger("CallSetup");
	ServletContext ctx = null;
	SipSessionsUtil sipSessionsUtil = null;

	   
    /** Processes requests for both HTTP <code>GET</code> 
     *  and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
	{
        String callA = null;
        String callB = null;
		String userNameA = null;
		String userNameB = null;
		
        String[] contacts = request.getParameterValues("CONTACT");
        String callback = request.getParameter("jsoncallback");
		
		response.setContentType("text/javascript");
        PrintWriter out = response.getWriter();
		
        if ( contacts == null || contacts.length != 2 ) 
		{
            out.println(callback + "({result:\"End call error, wrong number of parameters.\"});");
			out.close();
            return;
        }
		
		try
		{
			int userIdA = -1;
			int userIdB = -1;

			userIdA = (int)Integer.parseInt(contacts[0]);
			userIdB = (int)Integer.parseInt(contacts[1]);
			
			String strSession = SqlManager.getCallSession(userIdA, userIdB, true);
			
			if(strSession != null && strSession != "")
			{
				sipSessionsUtil = (SipSessionsUtil) ctx.getAttribute("javax.servlet.sip.SipSessionsUtil");
				
				SipApplicationSession as = sipSessionsUtil.getApplicationSessionById(strSession);
				
				if(as != null)
				{
					List<SipSession> sipSessions = getSipSessions(as);
					
					if(sipSessions != null)
					{
						SipSession sipSession = null;
						SipServletRequest requestBye = null;
						
						for(int i=0; i < sipSessions.size(); i++)
						{
							sipSession = (SipSession)sipSessions.get(i);
							
							try
							{
								requestBye = sipSession.createRequest("BYE");
								requestBye.send();
							}
							catch(Exception e)
							{
								logger.log(Level.SEVERE, "EndCallServlet 1: processRequest error: " + e);
							}
							sipSession.invalidate();
						}
					}
					as.invalidate();
				}
			}
			
            out.println(callback + "({result:\"OK\"});");
			out.close();
		}
		catch(Exception e)
		{
			out.println(callback + "({result:\""+e.getMessage()+"\"});");
			out.close();
			
			logger.log(Level.SEVERE, "EndCallServlet 2: processRequest error: " + e);
		}
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
    
    
    @Override
    public void init(ServletConfig config) throws ServletException
    {
		super.init(config);
		ctx = config.getServletContext();
		logger.log(Level.INFO, "Started EndCallServlet.");
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

