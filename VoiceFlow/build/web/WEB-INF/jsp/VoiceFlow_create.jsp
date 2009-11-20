<div>
<form action="<portlet:actionURL><portlet:param name="page" value="mainview"/></portlet:actionURL>" method="POST">
    <input name="pageDisplay" value="sendRequest" type="hidden" />
    <input name="userId" value="<%=userId%>" type="hidden" />
    <table>
            <tr>
                <td>
                    Insert request text:
                </td>
            </tr>
            <tr>
                <td>
                    <textarea name="textRequest" rows="7" cols="40" wrap="hard"></textarea>
                </td>
            </tr>
            <tr>
                <td>
                    <BR>
                </td>
            </tr>
            <tr>
                <td>
                    Select destination:
                </td>
            </tr>
            <tr>
                <td>
                    <select name="destinationId">

                    <%
                        String strMembersList = voiceFlowBean.getMembers((int)themeDisplay.getUserId());

                        if(strMembersList != null)
                        {
                            String[] st = strMembersList.split(voiceFlowBean.TOKEN_SEPARATOR);

                            for(int i=0; i<st.length; i++)
                            {
                                String[] stTemp = st[i].split(voiceFlowBean.WORD_SEPARATOR);
                    %>

                              <option value="<%=stTemp[0]%>"><%=stTemp[1]%></option>
                    <%
                            }
                        }
                    %>

                    </select>
                </td>
            </tr>
            <tr>
                <td>
                   <BR>
                </td>
            </tr>
            <tr>
                <td>
                    Submit:
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" value="Send request" />
                </td>
            </tr>
    </table>
<%--
    <BR>LogValue: <%=voiceFlowBean.getLogValueWeb() %>
--%>
</form>
</div>
