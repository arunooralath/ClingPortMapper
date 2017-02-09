package client;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.fourthline.cling.transport.RouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.cling.ClingRouterFactory;

public class PortMapperCli {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String routerFactoryClassName = ClingRouterFactory.class.getName();
	private Integer routerIndex = null;
	// private String internalClient;
	IRouter router = null;

	public PortMapperCli() {
		try {
			router = connect();
			if (router == null) {
				logger.error("No router found: exit");
				router.disconnect();
				throw new IllegalArgumentException("No Router Found");
			}
		} catch (RouterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (NullPointerException e) {
			throw new IllegalArgumentException("No Router Found");
		}
		try {
			printStatus(router);
			// printPortForwardings(router);
		} catch (RouterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// printStatus(router);
		// deletePortForwardings(router);
		// printPortForwardings(router);

	}

	@SuppressWarnings("unused")
	private void printPortForwardings(final IRouter router) {
		Collection<PortMapping> mappings = null;
		try {
			mappings = router.getPortMappings();
		} catch (RouterException e) {
			logger.warn("Router Exception");
		}
		// catch (ClingRouterException e) {
		// logger.warn("Router Exception1111111");
		// }
		if (mappings.size() == 0) {
			logger.info("No port mappings found");
			return;
		}
		final StringBuilder b = new StringBuilder();
		for (final Iterator<PortMapping> iterator = mappings.iterator(); iterator.hasNext();) {
			final PortMapping mapping = iterator.next();
			b.append(mapping.getCompleteDescription());
			if (iterator.hasNext()) {
				b.append("\n");
			}
		}
		logger.info("Found " + mappings.size() + " port forwardings:\n" + b.toString());
	}

	private boolean isAlreadyMapped(String internalClient, int port, String protocol) {
		Collection<PortMapping> mappings2 = null;
		boolean ret = false;
		try {
			mappings2 = router.getPortMappings();
		} catch (RouterException e) {
			logger.warn("Router Exception");
			ret = false;
		}
		if (mappings2.size() == 0) {
			logger.info("No port mappings found");
			ret = false;
		}
		System.out.println("Found " + mappings2.size() + " Mappings");

		for (PortMapping iterable_element : mappings2) {

			if (iterable_element.getInternalClient() != internalClient && iterable_element.getInternalPort() != port
					&& iterable_element.getProtocol().toString() != protocol) {
				continue;
			} else
				logger.info("Mapping already exists");
			ret = true;
			break;

		}
		return ret;
	}

	public void addMappings(String internalClient, int port, String protocol) {

		if (!isAlreadyMapped(internalClient, port, protocol)) {
			addPortForwarding(router, internalClient, port, protocol, "Sendrop");
		}

		// return false;

	}

	@SuppressWarnings("unused")
	private void deletePortForwardings(final IRouter router, String remoteHost, int port) {

		final String _remoteHost = remoteHost;
		final int _port = port;
		final Protocol protocol = Protocol.getProtocol("TCP");
		logger.info("Deleting mapping for protocol " + protocol + " and external port " + _port);
		try {
			router.removePortMapping(protocol, remoteHost, _port);
		} catch (RouterException e) {
			throw new IllegalArgumentException("Delete Mapping Failed");
		}
		// printPortForwardings(router);
	}

	private void printStatus(final IRouter router) throws RouterException {
		router.logRouterInfo();
	}

	private void addPortForwarding(final IRouter router, String internalClient, int port, String _protocol,
			String desc) {

		final String remoteHost = null;
		final int internalPort = port;
		final int externalPort = port;
		final Protocol protocol = Protocol.getProtocol(_protocol);
		final String description = desc;
		final PortMapping mapping = new PortMapping(protocol, remoteHost, externalPort, internalClient, internalPort,
				description);
		logger.info("Adding mapping " + mapping);
		try {
			router.addPortMapping(mapping);
		} catch (client.cling.ClingRouterException | RouterException e) {
			router.disconnect();
			throw new IllegalArgumentException("Add Mapping Failed");
		}
		// printPortForwardings(router);
	}

	@SuppressWarnings("unchecked")
	private AbstractRouterFactory createRouterFactory() throws RouterException {
		Class<AbstractRouterFactory> routerFactoryClass;
		logger.info("Creating router factory for class {}", routerFactoryClassName);
		try {
			routerFactoryClass = (Class<AbstractRouterFactory>) Class.forName(routerFactoryClassName);
		} catch (final ClassNotFoundException e) {
			throw new RouterException("Did not find router factory class for name " + routerFactoryClassName, e);
		}
		logger.debug("Creating a new instance of the router factory class {}", routerFactoryClass);
		try {
			final Constructor<AbstractRouterFactory> constructor = routerFactoryClass.getConstructor();
			return constructor.newInstance();
		} catch (final Exception e) {
			throw new RouterException("Error creating a router factory using class " + routerFactoryClass.getName(), e);
		}
	}

	private IRouter connect() throws RouterException {
		AbstractRouterFactory routerFactory;
		try {
			routerFactory = createRouterFactory();
		} catch (final RouterException e) {
			logger.error("Could not create router factory", e);
			return null;
		}
		logger.info("Searching for routers...");

		final List<IRouter> foundRouters = routerFactory.findRouters();

		return selectRouter(foundRouters);
	}

	/**
	 * @param foundRouters
	 * @return
	 */
	private IRouter selectRouter(final List<IRouter> foundRouters) {
		// One router found: use it.
		if (foundRouters.size() == 1) {
			final IRouter router = foundRouters.iterator().next();
			logger.info("Connected to router " + router.getName());
			return router;
		} else if (foundRouters.size() == 0) {
			logger.error("Found no router");
			return null;
		} else if (foundRouters.size() > 1 && routerIndex == null) {
			// let user choose which router to use.
			logger.error("Found more than one router. Use option -i <index>");

			int index = 0;
			for (final IRouter iRouter : foundRouters) {
				logger.error("- index " + index + ": " + iRouter.getName());
				index++;
			}
			return null;
		} else if (routerIndex >= 0 && routerIndex < foundRouters.size()) {
			final IRouter router = foundRouters.get(routerIndex);
			logger.info("Found more than one router, using " + router.getName());
			return router;
		} else {
			logger.error("Index must be between 0 and " + (foundRouters.size() - 1));
			return null;
		}
	}

}
