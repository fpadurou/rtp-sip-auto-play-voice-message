/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bean;

import java.io.Serializable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 *
 * @author Adrian
 */
public class VoiceFlowBean implements Serializable
{
    // --- make a HTTP Request to Sailfin to find out
    private static final String m_strSailfinUrl = "http://localhost:9090/CallSetup/";
    //private static final String m_strSailfinUrl = "http://75.101.141.22:9090/CallSetup/";
    
    public static String logValue;
    public static String logValueWeb;
    private String textRequest   = "textRequest";
    private String destinationId = "destinationId";
    private String userId        = "userId";

    // Mail variables
    public static boolean bSendMail = false;
    public static boolean bAccepted = false;
    public static long    nUserId   = -1;
    public static String  strMailText = "";

    // Error message
    public static String errorMessage;

    public static final String TOKEN_SEPARATOR = "_token_";
	public static final String WORD_SEPARATOR = "_word_";

    public String getErrorMessage() {
        return VoiceFlowBean.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
       VoiceFlowBean.errorMessage = errorMessage;
    }

    public static String getLogValueWeb() {
        return logValueWeb;
    }

    public static void setLogValueWeb(String logValueWeb) {
        VoiceFlowBean.logValueWeb = logValueWeb;
    }

    public static String getLogValue() {
        return logValue;
    }

    public static void setLogValue(String logValue) {
        VoiceFlowBean.logValue = logValue;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getTextRequest() {
        return textRequest;
    }

    public void setTextRequest(String textRequest) {
        this.textRequest = textRequest;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public static boolean sendRequest(String senderId, String receiverId, String strText)
	{
        logValue = "";
        try
        {
            strText = strText.replace('\'','a');
            strText = strText.replace('\r',' ');
            strText = strText.replace('\n',' ');

            // Construct data
            String data = "ACTION=NEW&CONTACT="+senderId+"&CONTACT="+receiverId+"&TEXT=";
            data += URLEncoder.encode(strText, "UTF-8");

            // Send data
            URL url = new URL(m_strSailfinUrl + "VoiceFlowServlet?" + data);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            line = rd.readLine();

            wr.close();
            rd.close();

            if(line.contains("[OK]"))
                return true;

            errorMessage = line;
        }
        catch(Exception e)
        {
            errorMessage = "sendRequest : " + e;
        }
		return false;
	}

    public static boolean processRequests(String receiverId, String strRequests)
	{
        logValue = "";
        try
        {
            // Construct data
            String data = "ACTION=PROCESS_REQUEST&CONTACT="+receiverId+"&REQUEST=";
            data += URLEncoder.encode(strRequests, "UTF-8");

            // Send data
            URL url = new URL(m_strSailfinUrl + "VoiceFlowServlet?" + data);

logValueWeb = "url : " + url.toString();

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            line = rd.readLine();

            wr.close();
            rd.close();

            // --- place a call, if accepted

            if(line.contains("[OK]"))
                return true;

            errorMessage = line;
        }
        catch(Exception e)
        {
            errorMessage = "processRequests : " + e;
        }
		return false;
	}

    public static boolean call(String sourceId, String destId)
    {
        logValue = "";
        try
        {
            // Send data
            String data = "CONTACT="+sourceId+"&CONTACT="+destId+"&format=json&jsoncallback=c";
            URL url = new URL(m_strSailfinUrl + "SipCallsetupServlet?" + data);
            
            data = URLEncoder.encode(data, "UTF-8");

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            String strResponse = "";
            String line = "";
            while( (line = rd.readLine())!= null )
            {
                strResponse += line;
            }

            rd.close();

            if(strResponse.contains("OK"))
                return true;

            errorMessage = line;
        }
        catch(Exception e)
        {
            errorMessage = "call : " + e;
        }

        return false;
    }

    public static void sendMail(int userId, boolean accepted, String text)
    {
        // actually this just sets a flag for sending mail
        bAccepted = accepted;
        nUserId = userId;
        bSendMail = true;
        strMailText = text;
    }

    public static String getMembers(int userId)
	{
        String result = null;
        logValue = "";
        try
        {
            // Construct data
            String data = "ACTION=MEMBERS&CONTACT="+userId;

            // Send data
            URL url = new URL(m_strSailfinUrl + "VoiceFlowServlet?" + data);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = rd.readLine();
            result = line;
            while( (line = rd.readLine()) != null )
            {
                result += line;
            }
            wr.close();
            rd.close();
            return result;
        }
        catch(Exception e)
        {
            errorMessage = "getMembers : " + e;
        }

        return result;
	}

    public static String createRequestSentTable(int userId)
    {
        logValue = "";

        String result = null;
        String table = "";

table += "<style type=\"text/css\">";
table += "table.sample {";
table += "	border-width: 1px 1px 1px 1px;";
table += "	border-style: inset inset inset inset;";
table += "	border-color: gray gray gray gray;";
table += "}";
table += "table.sample th {";
table += "	border-width: 1px 1px 1px 1px;";
table += "	padding: 1px 1px 1px 1px;";
table += "	border-style: inset inset inset inset;";
table += "	border-color: gray gray gray gray;";
table += "	background-color: #ddd;";
table += "	-moz-border-radius: 0px 0px 0px 0px;";
table += "}";
table += "table.sample td {";
table += "	border-width: 1px 1px 1px 1px;";
table += "	padding: 3px 3px 3px 3px;";
table += "	border-style: inset inset inset inset;";
table += "	border-color: gray gray gray gray;";
table += "	-moz-border-radius: 0px 0px 0px 0px;";
table += "}";
table += "</style>";

        table += "<table class=\"sample\">";
        table += "<THEAD>";

        table += "<tr>";
        table += "<th>To</th>";
        table += "<th>Request</th>";
        table += "<th>Status</th>";
        table += "</tr>";

        table += "</THEAD>";

        try
        {
            // Construct data
            String data = "ACTION=REQUESTS_SENT&CONTACT="+userId;

            // Send data
            URL url = new URL(m_strSailfinUrl + "VoiceFlowServlet?" + data);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = rd.readLine();
            result = line;
            while( (line = rd.readLine()) != null )
            {
                result += line;
            }
            wr.close();
            rd.close();

            String[] st = result.split(TOKEN_SEPARATOR);

            for(int i=0; i<st.length; i++)
            {
                String[] stTemp = st[i].split(WORD_SEPARATOR);
                table += "<tr>";
                table += "<td>"+stTemp[0]+"</td>";
                table += "<td>"+stTemp[1]+"</td>";
                table += "<td>"+stTemp[2]+"</td>";
                table += "</tr>";
            }
        }
        catch(Exception e)
        {
            errorMessage = "createRequestSentTable : " + e;
        }

        table += "</table>";

        return table;
    }

    public static String createRequestReceivedTable(int userId)
    {
        logValue = "";

        String result = null;
        String table = "";

table += "<style type=\"text/css\">";
table += "table.sample {";
table += "	border-width: 1px 1px 1px 1px;";
table += "	border-style: inset inset inset inset;";
table += "	border-color: gray gray gray gray;";
table += "}";
table += "table.sample th {";
table += "	border-width: 1px 1px 1px 1px;";
table += "	padding: 1px 1px 1px 1px;";
table += "	border-style: inset inset inset inset;";
table += "	border-color: gray gray gray gray;";
table += "	background-color: #ddd;";
table += "	-moz-border-radius: 0px 0px 0px 0px;";
table += "}";
table += "table.sample td {";
table += "	border-width: 1px 1px 1px 1px;";
table += "	padding: 3px 3px 3px 3px;";
table += "	border-style: inset inset inset inset;";
table += "	border-color: gray gray gray gray;";
table += "	-moz-border-radius: 0px 0px 0px 0px;";
table += "}";
table += "</style>";


        table += "<table class=\"sample\">";

        table += "<THEAD>";

        table += "<tr>";
        table += "<th></th>";
        table += "<th>From</th>";
        table += "<th>Request</th>";
        table += "<th align=center>Status</th>";
        table += "<th align=center>Accept</th>";
        table += "<th align=center>Deny</th>";
        table += "</tr>";

        table += "</THEAD>";

        try
        {
            // Construct data
            String data = "ACTION=REQUESTS_RECEIVED&CONTACT="+userId;

            // Send data
            URL url = new URL(m_strSailfinUrl + "VoiceFlowServlet?" + data);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            // returns user name, request text, status, request id, liferayUserId
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = rd.readLine();
            result = line;
            while( (line = rd.readLine()) != null )
            {
                result += line;
            }
            wr.close();
            rd.close();

            String[] st = result.split(TOKEN_SEPARATOR);

            for(int i=0; i<st.length; i++)
            {
                String[] stTemp = st[i].split(WORD_SEPARATOR);

                boolean isPending = stTemp[2].equalsIgnoreCase("pending");

                table += "<tr>";

                table += "<td>";

                if(isPending)
                {
                    table += "<input type=radio name=\"RequestSelect\" value=\"" + stTemp[3] + "\">";
                    table += "<input type=hidden name=\"RequestSelect" + stTemp[3] +"\" value=\"" + stTemp[4] + "\">";
                }
                else
                    table += "<input type=radio name=\"RequestSelect\" value=\"" + stTemp[3] + "\" disabled>";

                table += "</td>";

                // --- name
                table += "<td valign=\"middle\">"+stTemp[0]+"</td>";

                // --- text
                table += "<td>" + stTemp[1];
                table += "<input type=hidden name=\"RequestMessage" + stTemp[3] + "\" value=\"" + stTemp[1] + "\">";
                table += "</td>";

                // --- status
                table += "<td align=\"center\" valign=\"middle\">"+stTemp[2]+"</td>";

                // --- accept
                table += "<td align=\"center\" valign=\"middle\">";
                if(isPending)
                    table += "<input type=radio name=" + stTemp[3] + " value=\"accept\">";
                table += "</td>";

                // --- deny
                table += "<td align=\"center\" valign=\"middle\">";
                if(isPending)
                    table += "<input type=radio name=" + stTemp[3] + " value=\"deny\" checked>";
                table += "</td>";

                table += "</tr>";
            }
        }
        catch(Exception e)
        {
            errorMessage = "createRequestReceivedTable: " + e;
        }

        table += "</table>";
        return table;
    }
}
