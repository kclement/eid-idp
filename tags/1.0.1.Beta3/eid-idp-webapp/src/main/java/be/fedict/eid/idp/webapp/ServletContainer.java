package be.fedict.eid.idp.webapp;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.transport.http.servlet.ServletModule;

import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to {@link javax.servlet.ServletContext} via
 * {@link com.sun.xml.ws.api.server.Container}. Pipes can get ServletContext
 * from Container and use it to load some resources.
 */
class ServletContainer extends Container {
	private final ServletContext servletContext;

	private final ServletModule module = new ServletModule() {
		private final List<BoundEndpoint> endpoints = new ArrayList<BoundEndpoint>();

		public @NotNull
		List<BoundEndpoint> getBoundEndpoints() {
			return endpoints;
		}

		public @NotNull
		String getContextPath() {
			// Cannot compute this since we don't know about hostname and port
			// etc
			throw new WebServiceException("Container "
					+ ServletContainer.class.getName()
					+ " doesn't support getContextPath()");
		}
	};

	private final ResourceLoader loader = new ResourceLoader() {
		public URL getResource(String resource) throws MalformedURLException {
			return servletContext.getResource("/WEB-INF/" + resource);
		}
	};

	ServletContainer(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public <T> T getSPI(Class<T> spiType) {
		if (spiType == ServletContext.class) {
			return spiType.cast(servletContext);
		}
		if (spiType.isAssignableFrom(ServletModule.class)) {
			return spiType.cast(module);
		}
		if (spiType == ResourceLoader.class) {
			return spiType.cast(loader);
		}
		return null;
	}
}
