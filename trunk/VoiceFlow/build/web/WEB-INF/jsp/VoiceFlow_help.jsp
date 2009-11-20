<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<jsp:useBean id="voiceFlowBean" class="bean.VoiceFlowBean" scope="request"/>
<jsp:setProperty name="voiceFlowBean" property="*"/>

VoiceFlow error:
<BR>
<%=voiceFlowBean.getErrorMessage() %>
