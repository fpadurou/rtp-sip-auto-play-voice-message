<div>
    <form action="<portlet:actionURL><portlet:param name="page" value="mainview"/></portlet:actionURL>" method="POST">

        <input name="pageDisplay" value="updateRequest" type="hidden" />
        <input name="userId" value="<%=userId%>" type="hidden" />

        <%=voiceFlowBean.createRequestReceivedTable((int)themeDisplay.getUserId())%>


        <BR>
        <input type="submit" value="Process" />
<%--
   <BR>LogValue: <%=voiceFlowBean.getLogValueWeb() %>
--%>

    </form>
</div>