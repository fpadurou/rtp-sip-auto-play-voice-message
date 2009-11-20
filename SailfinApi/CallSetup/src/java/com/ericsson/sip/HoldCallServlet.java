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
 * Copyright (c) BlueSorcerer. All rights reserved.
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
import com.common.*;

/**
 *
 * @author BlueSorcerer
 * @version
 */

public class HoldCallServlet extends HttpServlet 
{
	private Logger logger = Logger.getLogger("CallSetup");
	ServletContext ctx = null;
	SipFactory sf = null;
	SipSessionsUtil sipSessionsUtil = null;
	
    /** Processes requests for both HTTP <code>GET</code> 
     *  and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
        String callA = null;
        String callB = null;
		
        String[] contacts = request.getParameterValues("CONTACT");
        String callback = request.getParameter("jsoncallback");
		String holdAction = request.getParameter("ACTION");
		
		response.setContentType("text/javascript");
        PrintWriter out = response.getWriter();
		
        if ( contacts == null || contacts.length != 2 || holdAction == null || !("HOLD".equals(holdAction) || "RESUME".equals(holdAction)) ) 
		{
            out.println(callback + "({result:\"Hold call error, wrong number of parameters or invalid call type.\"});");
			out.close();
            return;
        }

		try
		{
			int userIdA = -1;
			int userIdB = -1;

			userIdA = (int)Integer.parseInt(contacts[0]);
			userIdB = (int)Integer.parseInt(contacts[1]);
			
			String strSession = SqlManager.getCallSession(userIdA, userIdB, false);
			
			int nCallStatus = SqlManager.getStatus(userIdA, userIdB);

			boolean bOk = "HOLD".equals(holdAction) && nCallStatus == IStatus.STATUS_CALL_ACTIVE;
			boolean bCallOnHold = bOk;
			if(!bOk)
			{
				bOk = "RESUME".equals(holdAction) && (nCallStatus == IStatus.STATUS_CALL_ON_HOLD_BY_USER || nCallStatus == IStatus.STATUS_CALL_ON_HOLD_BY_FRIEND);
				
			}
			
			if(strSession != null && strSession != "" && bOk )
			{
				sipSessionsUtil = (SipSessionsUtil) ctx.getAttribute("javax.servlet.sip.SipSessionsUtil");
				
				SipApplicationSession as = sipSessionsUtil.getApplicationSessionById(strSession);
				
				if(as != null)
				{
					String strCallValid = (String)as.getAttribute("CallSession");
					
					if( !"VALID".equals(strCallValid) )
					{
						out.println(callback + "({result:\"Call still in progress...\"});");
						out.close();
						return;
					}
					else
					{
						List<SipSession> sipSessions = getSipSessions(as);
						
						// TODO improve this, no need for 2 sql connections
						String[] strSdp = new String[2];
						strSdp[0] = (String)sipSessions.get(0).getAttribute("SDP");
						strSdp[1] = (String)sipSessions.get(1).getAttribute("SDP");
						
// hold call 
logger.log(Level.INFO, "HoldCallServlet strSdp1: = " + strSdp[0] + "\n strSdp2 = " + strSdp[1]);				
						
						if(bCallOnHold)
						{
							if(strSdp[0] != null)
								strSdp[0] = strSdp[0].replaceAll("a=sendrecv","a=sendonly");
							if(strSdp[1] != null)
								strSdp[1] = strSdp[1].replaceAll("a=sendrecv","a=sendonly");
							as.setAttribute("HoldCall", "TRUE");
						}
						else
						{
							as.setAttribute("HoldCall", "FALSE");
						}

						SipSession sipSession = null;
						SipServletRequest requestInvite = null;
						
						for(int i=0; i < sipSessions.size(); i++)
						{
							if(strSdp[i] == null)
							{
								logger.log(Level.SEVERE, "strSdp["+i+"] is null.");
								continue;
							}
							
							sipSession = (SipSession)sipSessions.get(i);
							
							try
							{
								requestInvite = sipSession.createRequest("INVITE");
								requestInvite.setContent(strSdp[i], "application/sdp");
								requestInvite.send();
								logger.log(Level.INFO, "HoldCallServlet requestInvite = " + requestInvite.toString());
							}
							catch(Exception e)
							{
								logger.log(Level.SEVERE, "HoldCallServlet 1: processRequest error: " + e);
							}
						}
						
						// --- mark call status in database
						SqlManager.setStatus(userIdA, userIdB, bCallOnHold);
					}
				}
			}
			else
			{
				logger.log(Level.INFO, "HoldCallServlet: same event hold - hold or resume - resume: ");
			}
			
            out.println(callback + "({result:\"OK\"});");
			out.close();
		}
		catch(Exception e)
		{
			out.println(callback + "({result:\""+e.getMessage()+"\"});");
			out.close();
			
			logger.log(Level.SEVERE, "HoldCallServlet 2: processRequest error: " + e);
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
		
		try
		{
			sf = (SipFactory) ctx.getAttribute(SipServlet.SIP_FACTORY);
			logger.log(Level.INFO, "Started HoldCallServlet.");
		}
		catch(Throwable t)
		{
			logger.log(Level.SEVERE,"Could not start HoldCallServlet: ", t);
		}
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

