package com.voice;
import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.IOException;
import javax.portlet.PortletRequestDispatcher;

import bean.*;
/**
 * VoiceFlow Portlet Class
 */
public class VoiceFlow extends GenericPortlet
{
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException
    {
        String[] strValues = request.getParameterValues("pageDisplay");

        if(strValues != null)
        {
            String userId = request.getParameterValues("userId")[0];
            if(strValues[0].equalsIgnoreCase("sendRequest"))
            {
                try
                {
                    // --- we have a send request, gather information and process them
                    String textRequest = request.getParameterValues("textRequest")[0];
                    textRequest = textRequest.replace('\'', ' ');
                    String destinationId = request.getParameterValues("destinationId")[0];

                    if(VoiceFlowBean.sendRequest(userId, destinationId, textRequest))
                        response.setRenderParameter("requestValid", "true");
                    else
                        response.setRenderParameter("requestValid", "false");

VoiceFlowBean.logValueWeb = "'" + textRequest + "'";
                }
                catch(Exception e)
                {
                    VoiceFlowBean.errorMessage = "VoiceFlow : sendRequest : " + e;
                    response.setRenderParameter("requestValid", "false");
                }
            }
            else if(strValues[0].equalsIgnoreCase("updateRequest"))
            {
                try
                {
                    String[] requestIds = request.getParameterValues("RequestSelect");

                    if(requestIds != null && requestIds.length > 0)
                    {
                        String strRequest = "";

                        for(int i=0; i<requestIds.length; i++)
                        {
                            strRequest += requestIds[i] + VoiceFlowBean.WORD_SEPARATOR + request.getParameterValues(requestIds[i])[0] + VoiceFlowBean.TOKEN_SEPARATOR;
                        }

                        if(VoiceFlowBean.processRequests(userId, strRequest))
                        {
                            response.setRenderParameter("requestValid", "true");

                            String destinationUserId = request.getParameterValues("RequestSelect" + requestIds[0])[0];
                            String requestMessage = request.getParameterValues("RequestMessage" + requestIds[0])[0];

                            // --- if accepted, place a call, else send a mail
                            if("accept".equalsIgnoreCase(request.getParameterValues(requestIds[0])[0]))
                            {
                                // --- if call fails, send mail
                                if( !VoiceFlowBean.call(userId, destinationUserId) )
                                {
VoiceFlowBean.logValueWeb = "request accepted, call failed, sending mail...";
                                    VoiceFlowBean.sendMail(Integer.parseInt(destinationUserId), true, requestMessage);
                                }
                            }
                            else
                            {
                                VoiceFlowBean.sendMail(Integer.parseInt(destinationUserId), false, requestMessage);
VoiceFlowBean.logValueWeb = "request denied, sending mail...";
                            }
                        }
                        else
                            response.setRenderParameter("requestValid", "false");
                    }
                    else
                        response.setRenderParameter("requestValid", "true");

                }
                catch(Exception e)
                {
                    VoiceFlowBean.errorMessage = "VoiceFlow : updateRequest : " + e;
                    response.setRenderParameter("requestValid", "false");
                }
            }
        }
    }

    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException
    {
        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher = null;
           
        String requestValid = request.getParameter("requestValid");

        if(requestValid != null)
        {
            if(requestValid.equalsIgnoreCase("true"))
                dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/VoiceFlow_view.jsp");
            else
                dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/VoiceFlow_help.jsp");
        }
        else
            dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/VoiceFlow_view.jsp");
        dispatcher.include(request, response);
    }
    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/VoiceFlow_edit.jsp");
        dispatcher.include(request, response);
    }
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/VoiceFlow_help.jsp");
        dispatcher.include(request, response);
    }
}