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

package com.ericsson.sip.auth;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.*;
import javax.servlet.http.*;
import com.sqlmanager.*;

/**
 *
 * @author Sorcerer
 * @version
 */
 
public class LoginUserServlet extends HttpServlet
{	
	private Logger logger = Logger.getLogger("CallSetup");

    /** Processes requests for both HTTP <code>GET</code> 
     *  and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
	{
 		response.setContentType("text/javascript");
		
		String[] strLiferayId = request.getParameterValues("USERID");
		String[] strSession = request.getParameterValues("SESSION");
				
		if(strLiferayId == null || strSession == null || strLiferayId.length != 1 || strSession.length != 1)
		{
			// TODO: close it with error treatment
			return;
		}
		int result = -1;
		
		try
		{
			int nLiferayId = (int)Integer.parseInt(strLiferayId[0]);
			result = SqlManager.loginUser(nLiferayId, strSession[0]);
		}
		catch(Exception e)
		{
			// TODO: close it with error treatment
		}

		PrintWriter out = response.getWriter();
		out.println(result);
        out.close();
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
    }
}

