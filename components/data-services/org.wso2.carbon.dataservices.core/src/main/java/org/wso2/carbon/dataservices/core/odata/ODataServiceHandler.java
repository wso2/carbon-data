/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.dataservices.core.odata;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.apache.olingo.server.core.OData4Impl;
import org.wso2.carbon.dataservices.core.DataServiceFault;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * This class pass the OData requests into Olingo ODataHTTPHandler to process.
 */
public class ODataServiceHandler {
	/**
	 * Olingo ODataHTTPHandler
	 */
	private ODataHttpHandler handler;

	public ODataServiceHandler(ODataDataHandler dataHandler, String namespace, String configID) throws DataServiceFault {
		ODataAdapter processor = new ODataAdapter(dataHandler, namespace, configID);
		OData odata = OData4Impl.newInstance();
		ServiceMetadata edm = odata.createServiceMetadata(processor.getEdmProvider(),
		                                                  new ArrayList<EdmxReference>());
		this.handler = odata.createHandler(edm);
		this.handler.register(processor);
	}

	public void process(HttpServletRequest req, HttpServletResponse resp, String serviceRootPath) {
		handler.process(modifyServletPath(req, serviceRootPath), resp);
	}
//todo add comments
	private HttpServletRequest modifyServletPath(final HttpServletRequest req, final  String serviceRootPath) {

		return new HttpServletRequest() {
			@Override
			public String getAuthType() {
				return req.getAuthType();
			}

			@Override
			public Cookie[] getCookies() {
				return req.getCookies();
			}

			@Override
			public long getDateHeader(String s) {
				return req.getDateHeader(s);
			}

			@Override
			public String getHeader(String s) {
				return req.getHeader(s);
			}

			@Override
			public Enumeration<String> getHeaders(String s) {
				return req.getHeaders(s);
			}

			@Override
			public Enumeration<String> getHeaderNames() {
				return req.getHeaderNames();
			}

			@Override
			public int getIntHeader(String s) {
				return req.getIntHeader(s);
			}

			@Override
			public String getMethod() {
				return req.getMethod();
			}

			@Override
			public String getPathInfo() {
				return req.getPathInfo();
			}

			@Override
			public String getPathTranslated() {
				return req.getPathTranslated();
			}

			@Override
			public String getContextPath() {
				return req.getContextPath();
			}

			@Override
			public String getQueryString() {
				return req.getQueryString();
			}

			@Override
			public String getRemoteUser() {
				return req.getRemoteUser();
			}

			@Override
			public boolean isUserInRole(String s) {
				return req.isUserInRole(s);
			}

			@Override
			public Principal getUserPrincipal() {
				return req.getUserPrincipal();
			}

			@Override
			public String getRequestedSessionId() {
				return req.getRequestedSessionId();
			}

			@Override
			public String getRequestURI() {
				return req.getRequestURI();
			}

			@Override
			public StringBuffer getRequestURL() {
				return req.getRequestURL();
			}

			@Override
			public String getServletPath() {
				return req.getServletPath() + serviceRootPath;
			}

			@Override
			public HttpSession getSession(boolean b) {
				return req.getSession(b);
			}

			@Override
			public HttpSession getSession() {
				return req.getSession();
			}

			@Override
			public boolean isRequestedSessionIdValid() {
				return req.isRequestedSessionIdValid();
			}

			@Override
			public boolean isRequestedSessionIdFromCookie() {
				return req.isRequestedSessionIdFromCookie();
			}

			@Override
			public boolean isRequestedSessionIdFromURL() {
				return req.isRequestedSessionIdFromURL();
			}

			@Override
			public boolean isRequestedSessionIdFromUrl() {
				return req.isRequestedSessionIdFromUrl();
			}

			@Override
			public boolean authenticate(HttpServletResponse httpServletResponse)
					throws IOException, ServletException {
				return req.authenticate(httpServletResponse);
			}

			@Override
			public void login(String s, String s1) throws ServletException {
				req.login(s, s1);
			}

			@Override
			public void logout() throws ServletException {
				req.logout();
			}

			@Override
			public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
				return req.getParts();
			}

			@Override
			public Part getPart(String s) throws IOException, IllegalStateException, ServletException {
				return req.getPart(s);
			}

			@Override
			public Object getAttribute(String s) {
				return req.getAttribute(s);
			}

			@Override
			public Enumeration<String> getAttributeNames() {
				return req.getAttributeNames();
			}

			@Override
			public String getCharacterEncoding() {
				return req.getCharacterEncoding();
			}

			@Override
			public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
				req.setCharacterEncoding(s);
			}

			@Override
			public int getContentLength() {
				return req.getContentLength();
			}

			@Override
			public String getContentType() {
				return req.getContentType();
			}

			@Override
			public ServletInputStream getInputStream() throws IOException {
				return req.getInputStream();
			}

			@Override
			public String getParameter(String s) {
				return req.getParameter(s);
			}

			@Override
			public Enumeration<String> getParameterNames() {
				return req.getParameterNames();
			}

			@Override
			public String[] getParameterValues(String s) {
				return req.getParameterValues(s);
			}

			@Override
			public Map<String, String[]> getParameterMap() {
				return req.getParameterMap();
			}

			@Override
			public String getProtocol() {
				return req.getProtocol();
			}

			@Override
			public String getScheme() {
				return req.getScheme();
			}

			@Override
			public String getServerName() {
				return req.getServerName();
			}

			@Override
			public int getServerPort() {
				return req.getServerPort();
			}

			@Override
			public BufferedReader getReader() throws IOException {
				return req.getReader();
			}

			@Override
			public String getRemoteAddr() {
				return req.getRemoteAddr();
			}

			@Override
			public String getRemoteHost() {
				return req.getRemoteHost();
			}

			@Override
			public void setAttribute(String s, Object o) {
				req.setAttribute(s, o);
			}

			@Override
			public void removeAttribute(String s) {
				req.removeAttribute(s);
			}

			@Override
			public Locale getLocale() {
				return req.getLocale();
			}

			@Override
			public Enumeration<Locale> getLocales() {
				return req.getLocales();
			}

			@Override
			public boolean isSecure() {
				return req.isSecure();
			}

			@Override
			public RequestDispatcher getRequestDispatcher(String s) {
				return req.getRequestDispatcher(s);
			}

			@Override
			public String getRealPath(String s) {
				return req.getRealPath(s);
			}

			@Override
			public int getRemotePort() {
				return req.getRemotePort();
			}

			@Override
			public String getLocalName() {
				return req.getLocalName();
			}

			@Override
			public String getLocalAddr() {
				return req.getLocalAddr();
			}

			@Override
			public int getLocalPort() {
				return req.getLocalPort();
			}

			@Override
			public ServletContext getServletContext() {
				return req.getServletContext();
			}

			@Override
			public AsyncContext startAsync() {
				return req.startAsync();
			}

			@Override
			public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
				return req.startAsync(servletRequest, servletResponse);
			}

			@Override
			public boolean isAsyncStarted() {
				return req.isAsyncStarted();
			}

			@Override
			public boolean isAsyncSupported() {
				return req.isAsyncSupported();
			}

			@Override
			public AsyncContext getAsyncContext() {
				return req.getAsyncContext();
			}

			@Override
			public DispatcherType getDispatcherType() {
				return req.getDispatcherType();
			}
		};
	}
}
