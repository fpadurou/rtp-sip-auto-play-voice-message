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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.http.*;

import com.sqlmanager.*;


/**
 *
 * @author Sreeram
 * @version
 */
public class RegistrationBrowserServlet extends HttpServlet 
{
   /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
   {
       response.setContentType("text/html;charset=UTF-8");
       PrintWriter out = response.getWriter();
	   
       out.println("<html>");
       out.println("<head>");
       out.println("<title>Web Call</title>");
       out.println("</head>");
       out.println("<body>");
       out.println("<br>");
       out.println("<FORM ACTION = \"/CallSetup/SipCallsetupServlet\" METHOD = POST>");
       out.println("<table >");
       out.println("<tr>");
	   out.println("<td style=\"border-width: 1px; border-style: ridge\"><B>User name:<B></td>");
	   out.println("<td style=\"border-width: 1px; border-style: ridge\"><B>SIP:<B></td>");
	   out.println("</tr>");

		try
		{
			ArrayList<String[]> users = SqlManager.getUsersOnline(); 
			
			String[] strUsers = null;
			for(int i=0; i<users.size(); i++)
			{
				strUsers = users.get(i);
				
				out.println("<tr>");
				
				// --- user name
				out.println("<td style=\"border-width: 1px; border-style: ridge\">");
				out.println("<INPUT TYPE=\"CHECKBOX\" NAME=\"CONTACT\"" + " VALUE=\"" + strUsers[0] + "\">" + strUsers[1]);
				out.println("</td>");

				// --- sip
				out.println("<td style=\"border-width: 1px; border-style: ridge\">" + strUsers[2] + "</td>");
				
				out.println("</tr>");
			}
		}
		catch(Exception e)
		{
			out.println(e.getMessage());
		}
   
		out.println("<tr><td align=\"center\" colspan=\"2\" style=\"border-width: 1px; border-style: ridge\"><INPUT TYPE=SUBMIT NAME=Submit VALUE=\"Call\"></td></tr>");
	   
		out.println("</table>");
       
		out.println("</FORM>");
       
		out.println("</body>");
		out.println("</html>");
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
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
       processRequest(request, response);
   }

   /** Returns a short description of the servlet.
    */
   public String getServletInfo() {
       return "Short description";
   }
}
   // </editor-fold>

   
