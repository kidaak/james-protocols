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

package org.apache.james.protocols.api;

import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;

/**
 * Session for a protocol. Every new connection generates a new session
 * 
 *
 */
public interface ProtocolSession {
   
    /**
     * Gets the context sensitive log for this session.
     * @return log, not null
     */
    Logger getLogger();
    
    
    /**
     * Return Map which can be used to store objects within a session
     * 
     * @return state
     */
    Map<String, Object> getState();
    
    
    /**
     * Returns Map that consists of the state of the {@link ProtocolSession} per connection
     *
     * @return map of the current {@link ProtocolSession} state per connection
     */
    Map<String,Object> getConnectionState();

    
    /**
     * Reset the state
     */
    void resetState();

    
    /**
     * Return the {@link InetSocketAddress} of the remote peer
     * 
     * @return address
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Return the {@link InetSocketAddress} of the local bound address
     * 
     * @return local
     */
    InetSocketAddress getLocalAddress();
    
    /**
     * Return the ID for the session
     * 
     * @return id
     */
    String getSessionID();

    /**
     * Define a response object to be used as reply for a too long input line
     * 
     * @return Response or null if no response should be written before closing the connection
     */
    Response newLineTooLongResponse();

    /**
     * Define a response object to be used as reply during a fatal error.
     * Connection will be closed after this response.
     * 
     * @return Response or null if no response should be written before closing the connection
     */
    Response newFatalErrorResponse();
    
    /**
     * Returns the user name associated with this interaction.
     *
     * @return the user name
     */
    String getUser();

    /**
     * Sets the user name associated with this interaction.
     *
     * @param user the user name
     */
    void setUser(String user);

    /**
     * Return true if StartTLS is supported by the configuration
     * 
     * @return supported
     */
    boolean isStartTLSSupported();
    
    /**
     * Return true if the starttls was started
     * 
     * @return true
     */
    boolean isTLSStarted();
    
    /**
     * Return the {@link ProtocolConfiguration} 
     * 
     * @return config
     */
    ProtocolConfiguration getConfiguration();

}
