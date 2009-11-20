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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sqlmanager.*;
import com.common.*;

/**
 *
 * @author BlueSorcerer
 * @version
 */
 
public class VoiceFlowServlet extends HttpServlet 
{
	private Logger logger = Logger.getLogger("CallSetup");
	
	private final String TOKEN_SEPARATOR = "_token_";
	private final String WORD_SEPARATOR = "_word_";

    /** Processes requests for both HTTP <code>GET</code> 
     *  and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String action = request.getParameter("ACTION");
        String[] contacts = request.getParameterValues("CONTACT");
      
		response.setContentType("text/html;charset=UTF-8");
     
		PrintWriter out = response.getWriter();
        
        if ( contacts == null)
		{
            out.println("ERROR Contact is null");
			out.close();
            return;
        }
		
		try
		{
//A			logger.log(Level.SEVERE, "VoiceFlowServlet: action = " + action + ", contacts[0] = " + contacts[0]);
			
			if("NEW".equals(action))
			{
				int userIdA = (int)Integer.parseInt(contacts[0]);
				int userIdB = (int)Integer.parseInt(contacts[1]);
				String strText = request.getParameter("TEXT"); 
				
//A				logger.log(Level.SEVERE, "VoiceFlowServlet 2: userIdA = " + userIdA + ", userIdB = " + userIdB + ", strText = " + strText);
				
				SqlManager.setRequest(userIdA, userIdB, strText);
				
				out.println("[OK]");
			}
			else if ("MEMBERS".equals(action))
			{
				int userIdA = (int)Integer.parseInt(contacts[0]);
				ArrayList<String[]> strMembersList = SqlManager.getMembers(userIdA);

//A				logger.log(Level.SEVERE, "VoiceFlowServlet MEMBERS: size = " + strMembersList.size());
				
				String[] strMembers = null;
				for(int i=0; i<strMembersList.size(); i++)
				{
					strMembers = strMembersList.get(i);
					
					out.println(strMembers[0] + WORD_SEPARATOR + strMembers[1] + TOKEN_SEPARATOR);
				}
			}
			else if ("REQUESTS_SENT".equals(action))
			{
				int userIdA = (int)Integer.parseInt(contacts[0]);
				ArrayList<String[]> strRequestsList = SqlManager.getRequests(userIdA, false);

//A				logger.log(Level.SEVERE, "VoiceFlowServlet MEMBERS: size = " + strMembersList.size());
				
				String[] strRequest = null;
				for(int i=0; i<strRequestsList.size(); i++)
				{
					strRequest = strRequestsList.get(i);
					
					out.println(strRequest[0] + WORD_SEPARATOR + strRequest[1] + WORD_SEPARATOR + strRequest[2] + TOKEN_SEPARATOR);
				}
			}
			else if ("REQUESTS_RECEIVED".equals(action))
			{
				int userIdA = (int)Integer.parseInt(contacts[0]);
				ArrayList<String[]> strRequestsList = SqlManager.getRequests(userIdA, true);

//A				logger.log(Level.SEVERE, "VoiceFlowServlet MEMBERS: size = " + strMembersList.size());
				
				String[] strRequest = null;
				for(int i=0; i<strRequestsList.size(); i++)
				{
					strRequest = strRequestsList.get(i);
					
					out.println(strRequest[0] + WORD_SEPARATOR + strRequest[1] + WORD_SEPARATOR + strRequest[2] + WORD_SEPARATOR + strRequest[3] + WORD_SEPARATOR + strRequest[4] + TOKEN_SEPARATOR);
				}
			}
			else if ("PROCESS_REQUEST".equals(action))
			{
				int userIdA = (int)Integer.parseInt(contacts[0]);
				String userRequest = request.getParameter("REQUEST");

	            String[] st = userRequest.split(TOKEN_SEPARATOR);

	            ArrayList<String> strDenyArray = new ArrayList<String>();
	            ArrayList<String> strAcceptArray = new ArrayList<String>();
	            
	            for(int i=0; i<st.length; i++)
	            {
	                String[] strTemp = st[i].split(WORD_SEPARATOR);
	                
	                if("DENY".equalsIgnoreCase(strTemp[1]))
	                {
	                	strDenyArray.add(strTemp[0]);
	                }
	                else if("ACCEPT".equalsIgnoreCase(strTemp[1]))
	                {
	                	strAcceptArray.add(strTemp[0]);
	                }
	            }				
				
	            SqlManager.executeRequest(userIdA, strAcceptArray, strDenyArray);
	            
	            out.println("[OK]");
			}
		}
		catch(Exception e)
		{
			out.println("ERROR = " + e.getMessage());
		}
		
		out.close();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
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
    }
}

