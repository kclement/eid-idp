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

package be.fedict.eid.idp.sp.protocol.saml2;

import be.fedict.eid.idp.sp.protocol.saml2.spi.AuthenticationRequestService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.AuthnRequest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.KeyStore;

/**
 * Generates and sends out a SAML v2.0 Authentication Request.
 * <p/>
 * <p/>
 * Configuration can be provided either by providing:
 * <ul>
 * <li><tt>AuthenticationRequestService</tt>: {@link AuthenticationRequestService}
 * to provide the IdP protocol entry point, SP response handling location,
 * SP identity for signing the * authentication request, relay state,...</li>
 * </ul>
 * or by provinding:
 * <ul>
 * <li><tt>SPDestination</tt> or <tt>SPDestinationPage</tt>: Service Provider
 * destination that will handle the returned SAML2 response. One of the 2
 * parameters needs to be specified.</li>
 * <li><tt>IdPDestination</tt>: SAML2 entry point of the eID IdP.</li>
 * </ul>
 */
public class AuthenticationRequestServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        private static final Log LOG = LogFactory
                .getLog(AuthenticationRequestServlet.class);

        public static final String REQUEST_ID_SESSION_ATTRIBUTE =
                AuthenticationRequestServlet.class.getName() + ".RequestID";
        public static final String RECIPIENT_SESSION_ATTRIBUTE =
                AuthenticationRequestServlet.class.getName() + ".Recipient";
        public static final String RELAY_STATE_SESSION_ATTRIBUTE =
                AuthenticationRequestServlet.class.getName() + ".RelayState";

        private static final String AUTHN_REQUEST_SERVICE_PARAM =
                "AuthenticationRequestService";
        private static final String IDP_DESTINATION_PARAM =
                "IdPDestination";
        private static final String SP_DESTINATION_PARAM =
                "SPDestination";
        private static final String SP_DESTINATION_PAGE_PARAM =
                SP_DESTINATION_PARAM + "Page";
        private static final String LANGUAGE_PARAM =
                "Language";

        private String idpDestination;
        private String spDestination;
        private String spDestinationPage;
        private String language;

        private ServiceLocator<AuthenticationRequestService> authenticationRequestServiceLocator;

        @Override
        public void init(ServletConfig config) throws ServletException {

                this.idpDestination = config.getInitParameter(IDP_DESTINATION_PARAM);
                this.spDestination = config.getInitParameter(SP_DESTINATION_PARAM);
                this.spDestinationPage = config.getInitParameter(SP_DESTINATION_PAGE_PARAM);
                this.language = config.getInitParameter(LANGUAGE_PARAM);
                this.authenticationRequestServiceLocator = new
                        ServiceLocator<AuthenticationRequestService>
                        (AUTHN_REQUEST_SERVICE_PARAM, config);

                // validate necessary configuration params
                if (null == this.idpDestination
                        && !this.authenticationRequestServiceLocator.isConfigured()) {
                        throw new ServletException(
                                "need to provide either " + IDP_DESTINATION_PARAM
                                        + " or " + AUTHN_REQUEST_SERVICE_PARAM +
                                        "(Class) init-params");
                }

                if (null == this.spDestination && null == this.spDestinationPage
                        && !this.authenticationRequestServiceLocator.isConfigured()) {
                        throw new ServletException(
                                "need to provide either " + SP_DESTINATION_PARAM
                                        + " or " + SP_DESTINATION_PAGE_PARAM +
                                        " or " + AUTHN_REQUEST_SERVICE_PARAM +
                                        "(Class) init-param");
                }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws ServletException, IOException {
                LOG.debug("doGet");

                String issuer;
                String idpDestination;
                String spDestination;
                String relayState;
                KeyStore.PrivateKeyEntry spIdentity = null;
                String language;

                AuthenticationRequestService service =
                        this.authenticationRequestServiceLocator.locateService();
                if (null != service) {
                        issuer = service.getIssuer();
                        idpDestination = service.getIdPDestination();
                        relayState = service.getRelayState(request.getParameterMap());
                        spIdentity = service.getSPIdentity();
                        spDestination = service.getSPDestination();
                        language = service.getLanguage();
                } else {
                        idpDestination = this.idpDestination;
                        relayState = null;
                        if (null != this.spDestination) {
                                spDestination = this.spDestination;
                        } else {
                                spDestination = request.getScheme() + "://"
                                        + request.getServerName() + ":"
                                        + request.getServerPort() + request.getContextPath()
                                        + this.spDestinationPage;
                        }
                        issuer = spDestination;
                        language = this.language;
                }


                // generate and send an authentication request
                AuthnRequest authnRequest =
                        AuthenticationRequestUtil.sendRequest(issuer,
                                idpDestination, spDestination, relayState,
                                spIdentity, response, language);

                // save state on session
                setRequestId(authnRequest.getID(), request.getSession());
                setRecipient(authnRequest.getAssertionConsumerServiceURL(),
                        request.getSession());
                setRelayState(relayState, request.getSession());
        }

        /*
         * State handling
         */
        private void setRequestId(String requestId, HttpSession session) {
                session.setAttribute(REQUEST_ID_SESSION_ATTRIBUTE, requestId);
        }

        public static String getRequestId(HttpSession httpSession) {
                return (String) httpSession
                        .getAttribute(REQUEST_ID_SESSION_ATTRIBUTE);
        }

        private void setRecipient(String recipient, HttpSession session) {
                session.setAttribute(RECIPIENT_SESSION_ATTRIBUTE, recipient);
        }

        public static String getRecipient(HttpSession httpSession) {
                return (String) httpSession
                        .getAttribute(RECIPIENT_SESSION_ATTRIBUTE);
        }

        private void setRelayState(String relayState, HttpSession session) {
                session.setAttribute(RELAY_STATE_SESSION_ATTRIBUTE, relayState);
        }

        public static String getRelayState(HttpSession httpSession) {
                return (String) httpSession
                        .getAttribute(RELAY_STATE_SESSION_ATTRIBUTE);
        }

}
