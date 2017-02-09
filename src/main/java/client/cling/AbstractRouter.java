
package client.cling;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.fourthline.cling.transport.RouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.IRouter;

/**
 * This is the abstract super class for all routers.
 */
public abstract class AbstractRouter implements IRouter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String name;

	public AbstractRouter(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get the the ip of the local host.
	 */
	public String getLocalHostAddress() throws RouterException {
		logger.debug("Get IP of localhost");

		final InetAddress localHostIP = getLocalHostAddressFromSocket();

		// We do not want an address like 127.0.0.1
		if (localHostIP.getHostAddress().startsWith("127.")) {
			throw new RouterException("Only found an address that begins with '127.' when retrieving IP of localhost");
		}

		return localHostIP.getHostAddress();
	}

	/**
	 * Get the ip of the local host by connecting to the router and fetching the
	 * ip from the socket. This only works when we are connected to the router
	 * and know its internal upnp port.
	 *
	 * @return the ip of the local host.
	 * @throws RouterException
	 */
	private InetAddress getLocalHostAddressFromSocket() throws RouterException {
		InetAddress localHostIP = null;
		try {

			// In order to use the socket method to get the address, we have to
			// be connected to the router.
			final int routerInternalPort = getInternalPort();
			logger.debug("Got internal router port {}", routerInternalPort);

			// Check, if we got a correct port number
			if (routerInternalPort > 0) {
				logger.debug("Creating socket to router: {}:{}...", getInternalHostName(), routerInternalPort);
				try (Socket socket = new Socket(getInternalHostName(), routerInternalPort)) {
					localHostIP = socket.getLocalAddress();
				} catch (final UnknownHostException e) {
					throw new RouterException(
							"Could not create socked to " + getInternalHostName() + ":" + routerInternalPort, e);
				}

				logger.debug("Got address {} from socket.", localHostIP);
			} else {
				logger.debug("Got invalid internal router port number {}", routerInternalPort);
			}

			// We are not connected to the router or got an invalid port number,
			// so we have to use the traditional method.
			if (localHostIP == null) {

				logger.debug(
						"Not connected to router or got invalid port number, can not use socket to determine the address of the localhost. "
								+ "If no address is found, please connect to the router.");

				localHostIP = InetAddress.getLocalHost();

				logger.debug("Got address {} via InetAddress.getLocalHost().", localHostIP);
			}

		} catch (final IOException e) {
			throw new RouterException("Could not get IP of localhost.", e);
		}
		return localHostIP;
	}

	@Override
	public String toString() {
		return getName() + " (" + getInternalHostName() + ")";
	}
	
}
