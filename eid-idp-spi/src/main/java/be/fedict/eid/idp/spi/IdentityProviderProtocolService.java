/*
 * eID Identity Provider Project.
 * Copyright (C) 2010 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.fedict.eid.idp.spi;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * eID IdP Service Provider Interface for an identification/authentication
 * protocol. Protocol Services are stateless objects. State should be preserved
 * using the HTTP session context.
 *
 * @author Frank Cornelis
 */
public interface IdentityProviderProtocolService {

        /**
         * @return protocol specific ID.
         */
        String getId();

        /**
         * Initializes this protocol service handler.
         *
         * @param servletContext servlet context
         * @param configuration  IdP configuration
         */
        void init(ServletContext servletContext,
                  IdentityProviderConfiguration configuration);

        /**
         * Handles an incoming request for this protocol.
         *
         * @param request  the HTTP request.
         * @param response the HTTP response. Can be used if the protocol handler does
         *                 not want to continue via the regular IdP flow.
         * @return incoming request
         * @throws Exception in case this protocol service cannot handle the incoming
         *                   request.
         */
        IncomingRequest handleIncomingRequest(HttpServletRequest request,
                                              HttpServletResponse response)
                throws Exception;

        /**
         * Handles the outgoing response to return to the Service Provider web
         * application.
         *
         * @param httpSession the HTTP session context.
         * @param userId      user ID
         * @param attributes  returned attribute map
         * @param request     the HTTP request.
         * @param response    the HTTP response.   @return the response object in case a Browser POST should be constructed.
         *                    <code>null</code> in case this protocol service handles the
         *                    response generation itself.
         * @return response
         * @throws Exception in case this protocol service cannot construct the outgoing
         *                   response.
         */
        ReturnResponse handleReturnResponse(HttpSession httpSession, String userId,
                                            Map<String, Attribute> attributes,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
                throws Exception;

        /**
         * @param defaultAttribute default eID Attribute
         * @return protocol specific URI for this attribute or <code>null</code>
         *         if default uri is ok
         */
        String findAttributeUri(DefaultAttribute defaultAttribute);
}
