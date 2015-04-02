package com.gokrokve.murano.gateway.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.Token;
import org.openstack4j.openstack.OSFactory;

public class GatewayServlet extends HttpServlet {
	public static final String HEADER_X_AUTH_TOKEN = "X-Auth-Token";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_TYPE_JSON = "application/json";
	private Logger logger;
	private static final long serialVersionUID = 1L;
	private Properties params;
	private String murano_api_url;
	private String user;
	private String password;
	private String keystone_url;
	private String tenant;
	private Token tk;
	private Date token_time;
	private long token_expire;

	public GatewayServlet() {
		BasicConfigurator.configure();

		logger = Logger.getLogger(GatewayServlet.class);
		logger.debug("Initialize Gateway Servlet");
		params = new Properties();
		try {
			InputStream in = new FileInputStream(new File(
					"./etc/murano-gateway/gateway.properties"));
			params.load(in);
		} catch (IOException e) {
			logger.error("Can't read property file");
		}

		murano_api_url = params.getProperty("murano_api_url");
		user = params.getProperty("user");
		password = params.getProperty("password");
		keystone_url = params.getProperty("keystone_url");
		String tk_exp = params.getProperty("token_expire");
		tenant = params.getProperty("tenant");
		if (tk_exp == null) {
			token_expire = 120000L;
		} else {
			long tk_ex = Long.parseLong(tk_exp);
			token_expire = tk_ex * 1000; // Convert to milliseconds
		}
		logger.info(String.format("Murano API: %s", murano_api_url));
		logger.info(String.format("Keystone API: %s", keystone_url));
		logger.info(String.format("Default user: %s pwd: %s in tenant: %s",
				user, password, tenant));
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		response.setStatus(200);
		PrintWriter writer = response.getWriter();
		String url = request.getRequestURI();
		writer.println(String.format("Got reuqest to URL: %s", url));
		logger.info(String.format("Got GET request %s", url));
		if (!url.startsWith("/favicon.ico")) {
			this.forward(url, null);
		}
		writer.println("Request forwarded to Murano API");
		writer.close();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		response.setStatus(200);
		PrintWriter writer = response.getWriter();
		String url = request.getRequestURI();
		String body = IOUtils.toString(request.getInputStream());
		writer.println(String.format("Got reuqest to URL: %s", url));
		logger.info(String.format("Got GET request %s", url));
		if (!url.startsWith("/favicon.ico")) {
			this.forward(url, body);
		}
		writer.println("Request forwarded to Murano API");
		writer.close();
	}

	@SuppressWarnings("deprecation")
	private void forward(String url, String body) {
		HttpClient client = new HttpClient();

		// Create a method instance.
		PostMethod method = new PostMethod(murano_api_url + url);
		String token = getToken();
		method.addRequestHeader(HEADER_X_AUTH_TOKEN, token);
		method.setRequestHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
		if (body == null)
			method.setRequestBody("{}");
		else
			method.setRequestBody(body);
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.error(String
						.format("Unable to forward request %s: Server returned  non-OK status %d. Message: %s",
								url, statusCode,
								method.getResponseBodyAsString()));
			} else {
				logger.info(String.format(
						"Successfully forwarded the request for %s", url));
			}
		} catch (HttpException e) {
			logger.error(String.format("HTTP Error: %s", e.toString()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(String.format("HTTP Error: %s", e.toString()));
		}

	}

	private String getToken() {
		if (tk == null) {
			logger.info(String.format("No saved token. Getting a new one."));
			tk = getOSToken();
		} else {
			Date ctime = new Date();
			if ((ctime.getTime() - token_time.getTime()) > token_expire) {
				logger.info(String.format("Token is expired. Renewing a token."));
				tk = getOSToken();
			}
		}
		logger.info(String.format("Re-using cached valid non expired token"));
		return tk.getId();
	}

	private Token getOSToken() {
		OSClient os = OSFactory.builder().endpoint(keystone_url)
				.credentials(user, password).tenantName(tenant).authenticate();
		token_time = new Date();
		return os.getToken();
	}
}
