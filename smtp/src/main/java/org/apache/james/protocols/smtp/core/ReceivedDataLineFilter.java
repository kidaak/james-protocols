/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.protocols.smtp.core;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.handler.LineHandler;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.mailet.MailAddress;
import org.apache.mailet.base.RFC2822Headers;
import org.apache.mailet.base.RFC822DateFormat;

public class ReceivedDataLineFilter implements DataLineFilter {

    private final static String CHARSET = "US-ASCII";
    
    private final static String SOFTWARE_TYPE = "JAMES SMTP Server ";

    // Replace this with something usefull
    // + Constants.SOFTWARE_VERSION;

    /**
     * Static RFC822DateFormat used to generate date headers
     */
    private final static RFC822DateFormat rfc822DateFormat = new RFC822DateFormat();
    private final static String HEADERS_WRITTEN = "HEADERS_WRITTEN";


    /**
     * @see org.apache.james.protocols.smtp.core.DataLineFilter#onLine(SMTPSession, byte[], LineHandler)
     */
    public Response onLine(SMTPSession session,  byte[] line, LineHandler<SMTPSession> next) {
        if (session.getState().containsKey(HEADERS_WRITTEN) == false) {
            Response response = addNewReceivedMailHeaders(session, next);

            session.getState().put(HEADERS_WRITTEN, true);
            
            if (response != null) {
                return response;
            }
        }
        Response resp =  next.onLine(session, line);
        return resp;
    }

    @SuppressWarnings("unchecked")
    private Response addNewReceivedMailHeaders(SMTPSession session, LineHandler<SMTPSession> next) {
        try {
            StringBuilder headerLineBuffer = new StringBuilder();

            String heloMode = (String) session.getConnectionState().get(SMTPSession.CURRENT_HELO_MODE);
            String heloName = (String) session.getConnectionState().get(SMTPSession.CURRENT_HELO_NAME);

            // Put our Received header first
            headerLineBuffer.append(RFC2822Headers.RECEIVED + ": from ").append(session.getRemoteAddress().getHostName());

            if (heloName != null) {
                headerLineBuffer.append(" (").append(heloMode).append(" ").append(heloName).append(") ");
            }

            headerLineBuffer.append(" ([").append(session.getRemoteAddress().getAddress().getHostAddress()).append("])").append("\r\n");

            Response response = next.onLine(session, headerLineBuffer.toString().getBytes(CHARSET));
            if (response != null) {
                return response;
            }
            headerLineBuffer.delete(0, headerLineBuffer.length());

            headerLineBuffer.append("          by ").append(session.getHelloName()).append(" (").append(SOFTWARE_TYPE).append(") with ");

            // Check if EHLO was used
            if ("EHLO".equals(heloMode)) {
                // Not successful auth
                if (session.getUser() == null) {
                    headerLineBuffer.append("ESMTP");
                } else {
                    // See RFC3848
                    // The new keyword "ESMTPA" indicates the use of ESMTP when
                    // the
                    // SMTP
                    // AUTH [3] extension is also used and authentication is
                    // successfully
                    // achieved.
                    headerLineBuffer.append("ESMTPA");
                }
            } else {
                headerLineBuffer.append("SMTP");
            }

            headerLineBuffer.append(" ID ").append(session.getSessionID());

            if (((Collection<?>) session.getState().get(SMTPSession.RCPT_LIST)).size() == 1) {
                // Only indicate a recipient if they're the only recipient
                // (prevents email address harvesting and large headers in
                // bulk email)
                headerLineBuffer.append("\r\n");
                next.onLine(session, headerLineBuffer.toString().getBytes(CHARSET));
                headerLineBuffer.delete(0, headerLineBuffer.length());

                headerLineBuffer.delete(0, headerLineBuffer.length());
                headerLineBuffer.append("          for <").append(((List<MailAddress>) session.getState().get(SMTPSession.RCPT_LIST)).get(0).toString()).append(">;").append("\r\n");
                response = next.onLine(session, headerLineBuffer.toString().getBytes(CHARSET));

                if (response != null) {
                    return response;
                }
                headerLineBuffer.delete(0, headerLineBuffer.length());
                headerLineBuffer.delete(0, headerLineBuffer.length());

            } else {
                // Put the ; on the end of the 'by' line
                headerLineBuffer.append(";");
                headerLineBuffer.append("\r\n");

                response = next.onLine(session, headerLineBuffer.toString().getBytes(CHARSET));
                if (response != null) {
                    return response;
                }
                headerLineBuffer.delete(0, headerLineBuffer.length());
            }
            headerLineBuffer = null;
            return next.onLine(session, ("          " + rfc822DateFormat.format(new Date()) + "\r\n").getBytes(CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No US-ASCII support ?");
        }
    }
}
