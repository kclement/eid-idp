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

package be.fedict.eid.idp.sp.protocol.openid;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;

public class AuthenticationResponseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(AuthenticationResponseServlet.class);

	private String identifierSessionAttribute;

	private String redirectPage;

	private String nameSessionAttribute;

	private String firstNameSessionAttribute;

	@Override
	public void init(ServletConfig config) throws ServletException {
		this.identifierSessionAttribute = getRequiredInitParameter(
				"IdentifierSessionAttribute", config);
		this.redirectPage = getRequiredInitParameter("RedirectPage", config);
		this.nameSessionAttribute = config
				.getInitParameter("NameSessionAttribute");
		this.firstNameSessionAttribute = config
				.getInitParameter("FirstNameSessionAttribute");
	}

	private String getRequiredInitParameter(String parameterName,
			ServletConfig config) throws ServletException {
		String value = config.getInitParameter(parameterName);
		if (null == value) {
			throw new ServletException(parameterName
					+ " init-param is required");
		}
		return value;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doGet");
		String openIdMode = request.getParameter("openid.mode");
		if ("id_res".equals(openIdMode)) {
			try {
				doIdRes(request, response);
			} catch (Exception e) {
				throw new ServletException("OpenID error: " + e.getMessage(), e);
			}
		}
	}

	private void doIdRes(HttpServletRequest request,
			HttpServletResponse response) throws MessageException,
			DiscoveryException, AssociationException, IOException {
		LOG.debug("id_res");
		LOG.debug("request URL: " + request.getRequestURL());
		ParameterList parameterList = new ParameterList(request
				.getParameterMap());
		DiscoveryInformation discovered = (DiscoveryInformation) request
				.getSession().getAttribute("openid-disc");
		String receivingUrl = "https://" + request.getServerName() + ":"
				+ request.getLocalPort() + "/eid-idp-sp/openid-landing";
		String queryString = request.getQueryString();
		if (queryString != null && queryString.length() > 0) {
			receivingUrl += "?" + queryString;
		}
		LOG.debug("receiving url: " + receivingUrl);
		ConsumerManager consumerManager = AuthenticationRequestServlet
				.getConsumerManager(request);
		VerificationResult verificationResult = consumerManager.verify(
				receivingUrl.toString(), parameterList, discovered);
		Identifier identifier = verificationResult.getVerifiedId();
		if (null != identifier) {
			String userId = identifier.getIdentifier();
			LOG.debug("userId: " + userId);
			HttpSession httpSession = request.getSession();
			httpSession.setAttribute(this.identifierSessionAttribute, userId);
			Message authResponse = verificationResult.getAuthResponse();
			if (authResponse.hasExtension(AxMessage.OPENID_NS_AX)) {
				MessageExtension messageExtension = authResponse
						.getExtension(AxMessage.OPENID_NS_AX);
				if (messageExtension instanceof FetchResponse) {
					FetchResponse fetchResponse = (FetchResponse) messageExtension;
					String firstName = fetchResponse
							.getAttributeValueByTypeUri("http://axschema.org/namePerson/first");
					httpSession.setAttribute(this.firstNameSessionAttribute,
							firstName);
					String name = fetchResponse
							.getAttributeValueByTypeUri("http://axschema.org/namePerson/last");
					httpSession.setAttribute(this.nameSessionAttribute, name);
				}
			}
			response.sendRedirect(request.getContextPath() + this.redirectPage);
		} else {
			LOG.warn("no verified identifier");
		}
	}
}
