<jsp:useBean id="voiceFlowBean" class="bean.VoiceFlowBean" scope="request"/>
<jsp:setProperty name="voiceFlowBean" property="*"/>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>
<%@ page import="com.liferay.portal.kernel.util.PrefsPropsUtil" %>
<%@ page import="com.liferay.portal.service.UserLocalServiceUtil" %>
<%@ page import="com.liferay.portal.model.User" %>
<%@ page import="com.liferay.mail.service.MailServiceUtil" %>
<%@ page import="com.liferay.portal.kernel.mail.MailMessage" %>
<%@ page import="javax.mail.internet.InternetAddress" %>

<liferay-theme:defineObjects />
<portlet:defineObjects />

<liferay-theme:defineObjects />
<portlet:defineObjects />

<%
    if(!themeDisplay.isSignedIn())
    {
        %>
        <div>
            To access the portlet please login first.
        </div>
        <%
        return;
    }
    int userId = (int)themeDisplay.getUserId();
%>

<%
String tabs1 = ParamUtil.getString(request, "tabs1", "Create");

PortletURL portletURL = renderResponse.createRenderURL();

portletURL.setParameter("tabs1", tabs1);

String tabNames = "Create,Sent,Received";

%>

<liferay-ui:tabs
   names="<%= tabNames %>"
   url="<%= portletURL.toString() %>"
/>

<c:if test='<%= tabs1.equals("Create") %>'>
  <%@include file="./VoiceFlow_create.jsp" %>
</c:if>

<c:if test='<%= tabs1.equals("Sent") %>'>
  <%@include file="./VoiceFlow_sent.jsp" %>
</c:if>

<c:if test='<%= tabs1.equals("Received") %>'>
  <%@include file="./VoiceFlow_received.jsp" %>
</c:if>

<%
if(voiceFlowBean.nUserId > 0 && voiceFlowBean.bSendMail == true)
{
    // --- this part tries to send mail when the bean allows it
    try
    {
        voiceFlowBean.bSendMail = false;

        // --- get user to sent the mail
        User toUser = UserLocalServiceUtil.getUserById(voiceFlowBean.nUserId);
        String toName = toUser.getFullName();
        String toAddress = toUser.getEmailAddress();

        // --- set the mail's subject and body
        String subject = "Your request has been ";
        String body = "Hello " + toName + ", \n\n";

        if(!voiceFlowBean.bAccepted)
        {
            subject += "denied.";
            body += "Your request referenced below has been unfortunately denied.";
        }
        else
        {
            subject += "approved.";
            body += "Your request referenced below has been approved.\n\n";
        }

        body += "\"" + voiceFlowBean.strMailText + "\"";
        body += "\n\nAccentway Web Admin";

        
        // --- set user who is sending the mail
        String fromName = PrefsPropsUtil.getString(themeDisplay.getCompanyId(), "admin.email.from.name");
        String fromAddress = PrefsPropsUtil.getString(themeDisplay.getCompanyId(), "admin.email.from.address");
        
        // --- compose the mail
        MailMessage mailMessage = new MailMessage();

        InternetAddress from = new InternetAddress(fromAddress,fromName);
        InternetAddress to = new InternetAddress(toAddress, toName);

        mailMessage.setTo(new InternetAddress[]{to});

        mailMessage.setFrom(from);

        mailMessage.setBody(body);

        mailMessage.setSubject(subject);

        MailServiceUtil.sendEmail(mailMessage);
    %>
<%--
    To: <%=toName %>, <%=toAddress %>
        <BR>
    From: <%=fromName %>, <%=fromAddress %>
        <BR>
            Body: <%=body%>
--%>
    <%

        }catch(Exception ex){
    %>
        Error sending mail: <%=ex%>
    <%
        }
}
%>
