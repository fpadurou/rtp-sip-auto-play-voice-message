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
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.sip.*;


import java.util.logging.Logger;
import java.util.logging.Level;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;

import javax.transaction.UserTransaction;

import com.sqlmanager.*;
import com.common.*;

public class SipCallSetupServlet extends HttpServlet 
{
	private Logger logger = Logger.getLogger("CallSetup");
	SipFactory sf = null;
	ServletContext ctx = null;

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
		
		if(callback != null)
		{
			response.setContentType("text/javascript");
		}
		else
		{
			response.setContentType("text/html;charset=UTF-8");
		}
		
        PrintWriter out = response.getWriter();
		
		// --- web only
		if(callback == null)
		{
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servlet SipCallSetupServlet</title>");
			out.println("</head>");
			out.println("<body>");
		}
		
        if ( contacts == null || contacts.length != 2 ) 
		{
            out.println(callback + "({result:\"Call error, wrong number of parameters.\"});");
			
			if(callback == null)
			{
				out.println("</body>");
				out.println("</html>");
			}
			
			out.close();
            return;
        }
		
		try
		{
			// --- look in database for user sip address
			int userIdA = -1;
			int userIdB = -1;

			userIdA = (int)Integer.parseInt(contacts[0]);
			callA = SqlManager.getSipAddress(userIdA);
			userNameA = SqlManager.getUserName(userIdA);
			
			userIdB = (int)Integer.parseInt(contacts[1]);
			callB = SqlManager.getSipAddress(userIdB);
			userNameB = SqlManager.getUserName(userIdB);
			
			// --- make the call between users
			if(callA == null || callA == "")
			{
				out.println(callback + "({result:\"You are not online. Please login in X-Lite.\"});");
			}
			else if(callB == null || callB == "")
			{
				out.println(callback + "({result:\""+userNameB+" not online.\"});");
			}
			else if(callA.compareTo(callB) == 0)
			{
				out.println(callback + "({result:\"recursive call.\"});");
			}
			else
			{
				// --- check first is the users don't have a call in progress already
				String sesId = SqlManager.getCallSession(userIdA, userIdB, false);
	
				if(sesId == "")
				{
					sesId = doCall(callA, callB, userIdA, userIdB);
					SqlManager.setCallSession(userIdA, userIdB, sesId);
					SqlManager.setStatus(sesId, IStatus.STATUS_PROCESSING_REQUEST);
					
					if(callback != null)
					{
						out.println(callback + "({result:\"OK\"});");
					}
					else
					{
						out.println("Call between: " + callA + " and " + callB);
					}
				}
				else
				{
					logger.log(Level.WARNING, "SipCallSetupServlet Call between: " + callA + " and " + callB + ", sessId = " + sesId + " is already in progress.");
					
					if(callback != null)
					{
						out.println(callback + "({result:\"Call already in progress.\"});");
					}
					else
					{
						out.println("Call between: " + callA + " and " + callB + " is already in progress.");
					}					
				}
			}
			
			if(callback == null)
			{
				out.println("</body>");
				out.println("</html>");
			}
			
			out.close();
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "VOCE: call error: callA = " + callA + ", callB = " + callB + ", message: " + e);
			out.close();
		}
    }
    
	public String doCall(String callA, String callB, int userIdA, int userIdB) throws ServletException, IOException
	{
		SipApplicationSession as = sf.createApplicationSession();

        Address to = sf.createAddress(callB);
        if(to.getParameter("transport") != null ) 
		{
            ((SipURI)to.getURI()).setTransportParam(to.getParameter("transport"));
        }
        
        Address from = sf.createAddress(callA);
        if(from.getParameter("transport") != null ) 
		{
            ((SipURI)from.getURI()).setTransportParam(from.getParameter("transport"));
        }
        
        SipServletRequest sipReq = sf.createRequest(as, "INVITE", from, to);

        logger.log(Level.INFO, "SipCallSetupServlet sipRequest = " + sipReq.toString());

		// --- save user id's in sas, so we don't need to parse them from messages
		as.setAttribute("UserIdA", "" + userIdA);
		as.setAttribute("UserIdB", "" + userIdB);
       
		// --- set servlet to invoke by reponse
		SipSession s = sipReq.getSession();
		s.setHandler("b2b");

		// --- lets send invite to B ...
		sipReq.send();
		
		return as.getId();
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
    public String getServletInfo() 
	{
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
			logger.log(Level.INFO, "Started SipCallSetupServlet.");
		}
		catch(Throwable t)
		{
			logger.log(Level.SEVERE,"Could not start SipCallSetupServlet: ", t);
		}
    }
}

