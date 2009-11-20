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
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 */
package com.ericsson.sip;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.sip.*;
import com.sqlmanager.*;
import com.common.*;
import java.util.logging.*;

/**
 *
 * B2BUA scenario:
 * 
 *   Client                SailFin               Server
 * 
 *                       (from MDB)     INVITE -------->
 *                                           
 *                                      <-- 200OK (sdp1)
 *   <-- INVITE (sdp1)
 * 
 *   200OK (sdp2) --->
 * 
 *   <------------ ACK
 * 
 *                                      ACK (sdp2) ---->
 * 
 *  <------------------ RTP session ------------------->
 * 
 *  
 *  BYE ------------->
 * 
 *                                      BYE ----------->
 *                        
 *                                      <-------- 200 OK
 * 
 *  <---------- 200 OK
 *
 * Implementation hacks:
 * 
 * The ACK and 200OK are saved in the SipSession because the server currently
 * does not support B2BUAHelper.getPendingMessages(SipSession session, UAMode mode).
 * 
 * Once the server has that feature, then we can remove this hack.
 * 
 * @author bhavanishankar@dev.java.net
 */

public class B2BCallServlet extends javax.servlet.sip.SipServlet 
{
	private Logger logger = Logger.getLogger("CallSetup");

    @Override
    protected void doSuccessResponse(SipServletResponse resp)
            throws ServletException, IOException 
	{
        log("doSuccessResponse: Received a response.\n" + resp);
        if (resp.getMethod().equals("INVITE")) 
		{
			SipApplicationSession sas = resp.getApplicationSession();
			
            List<SipSession> sipSessions = getSipSessions(sas);
			
            if (sipSessions.size() == 1)
			{ 
				// 200 OK from Server
				SipSession sess = resp.getSession();
                sess.setAttribute("ACK", resp.createAck());
				
				// ---- set sdp1
				byte[] bContent = resp.getRawContent();
				String strContent = new String(bContent);
				sess.setAttribute("SDP", strContent);
				
				log("-------------------sdp1 content = " + strContent);
				
                sendInviteToClient(resp);
            }
			else 
			{
				String strHoldAttribute = (String)sas.getAttribute("HoldCall");

				// --- plain invite call
				if(strHoldAttribute == null)
				{
					sas.setAttribute("CallSession", "VALID");

					if(resp.getContent() != null)
					{
						SipSession sess = resp.getSession();
						
						// ---- set sdp2
						byte[] bContent = resp.getRawContent();
						String strContent = new String(bContent);
						sess.setAttribute("SDP", strContent);
						
						log("-------------------sdp2 content = " + strContent);
					}
					
					// --- call should be active now, update in database
					try
					{
						SqlManager.setStatus(sas.getId(), IStatus.STATUS_CALL_ACTIVE);
					}
					catch(Exception e)
					{
						logger.log(Level.SEVERE, "Setting call active error " + e);
					}
					
					// 200 OK from Client
					sendAckToClient(resp);
					sendAckToServer(resp);
				}
				// --- Hold Call invite
				else
				{
					sendAckToClient(resp);
				}
            }
        } 
		else if (resp.getMethod().equals("BYE"))
		{
            send200OKToClient(resp);
        }
    }

    @Override
    protected void doErrorResponse(SipServletResponse resp)
            throws ServletException, IOException 
	{
        log("doErrorResponse: Received an error response.\n" + resp);
        resp.getSession().getApplicationSession().invalidate();
    }

    @Override
    protected void doRequest(SipServletRequest req) throws ServletException, IOException 
	{
        
        if (req.getMethod().equals("BYE")) 
		{
            /**
             * If getPendingMessages() works then we just need to create 
             * the 200OK response and it will automatically be added to 
             * list of pending messages.
             * 
             * But as of now, we need to keep in in the SipSession.
             * 
             * The 200OK response can only be sent when the BYE request to the 
             * other party is successful.
             */
            req.getSession().setAttribute("BYE", req.createResponse(200));
            sendByeToServer(req);
        }
		else if(req.getMethod().equals("INVITE"))	// --- hold invite
		{
			SipServletResponse resp = req.createResponse(200);
			resp.send();
			logger.log(Level.INFO, "doRequest: Sent response.\n" + resp);
		}
    }

    private void sendInviteToClient(SipServletResponse serverResp)
            throws ServletException, IOException {
        SipServletRequest serverReq = serverResp.getRequest();
        B2buaHelper b2buaHelper = serverReq.getB2buaHelper();

        // Swap To & From headers.
        Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
        List<String> from = new ArrayList<String>();
        from.add(serverResp.getHeader("From"));
        headerMap.put("To", from);
        List<String> to = new ArrayList<String>();
        to.add(serverResp.getHeader("To"));
        headerMap.put("From", to);

        SipServletRequest clientRequest = b2buaHelper.createRequest(serverReq, true, headerMap);
        clientRequest.setRequestURI(clientRequest.getAddressHeader("To").getURI());
		
        if (serverResp.getContent() != null) 
		{ 	
			// set sdp1
            clientRequest.setContent(serverResp.getContent(), serverResp.getContentType());
        }
        logger.log(Level.INFO, "Sending INVITE to client.\n" + clientRequest);
        clientRequest.send();
    }

    private void sendAckToClient(SipServletResponse clientResp) throws ServletException, IOException 
	{
        SipServletRequest ack = clientResp.createAck();
        ack.send();
		
		logger.log(Level.INFO, "Sending ACK to client.\n" + ack);
    }

    private void sendAckToServer(SipServletResponse clientResp) throws ServletException, IOException 
	{
        B2buaHelper b2buaHelper = clientResp.getRequest().getB2buaHelper();
        SipSession clientSession = clientResp.getSession();
        SipSession serverSession = b2buaHelper.getLinkedSession(clientSession);
        SipServletRequest ack = (SipServletRequest) serverSession.getAttribute("ACK");
        serverSession.removeAttribute("ACK");
		
        if (clientResp.getContent() != null) 
		{ 
			// set sdp2
            ack.setContent(clientResp.getContent(), clientResp.getContentType());
        }
		
        ack.send();
    }

    private void sendByeToServer(SipServletRequest clientBye)
            throws ServletException, IOException {
        B2buaHelper b2buaHelper = clientBye.getB2buaHelper();
        SipSession serverSession = b2buaHelper.getLinkedSession(clientBye.getSession());
        SipServletRequest serverBye = serverSession.createRequest("BYE");
        log("Sending BYE request.\n" + serverBye);
        serverBye.send();
    }

    private void send200OKToClient(SipServletResponse serverResp)
            throws ServletException, IOException 
	{
        B2buaHelper b2buaHelper = serverResp.getRequest().getB2buaHelper();
        SipSession clientSession = b2buaHelper.getLinkedSession(serverResp.getSession());
        SipServletResponse clientResp = (SipServletResponse) clientSession.getAttribute("BYE");
        clientSession.removeAttribute("BYE");
        clientResp.setStatus(serverResp.getStatus(), serverResp.getReasonPhrase());
        log("Sending response.\n" + clientResp);
        clientResp.send();
        clientSession.getApplicationSession().invalidate();
    }

    private List<SipSession> getSipSessions(SipApplicationSession sas) {
        List<SipSession> sipSessions = new ArrayList<SipSession>();
        Iterator<SipSession> iter =
                (Iterator<SipSession>) sas.getSessions("SIP");
        while (iter.hasNext()) {
            sipSessions.add(iter.next());
        }
        return sipSessions;
    }
}
