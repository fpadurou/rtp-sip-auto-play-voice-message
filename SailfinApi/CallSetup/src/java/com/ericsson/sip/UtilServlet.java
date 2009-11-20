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
import com.common.*;

/**
 *
 * @author BlueSorcerer
 * @version
 */
 
public class UtilServlet extends HttpServlet 
{
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
			if("ONLINE".equals(action))
			{
				int userId = (int)Integer.parseInt(contacts[0]);
				
				String strSipAddress = SqlManager.getSipAddress(userId);
				
				if(strSipAddress.length() > 0)
				{
					out.println("YES");
				}
				else
				{
					out.println("NO");
				}
			}
			else if("GETSTATUS".equals(action))
			{
				int userIdA = (int)Integer.parseInt(contacts[0]);
				int userIdB = (int)Integer.parseInt(contacts[1]);
				
				int result = SqlManager.getStatus(userIdA, userIdB);
				out.println("" + result);
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

