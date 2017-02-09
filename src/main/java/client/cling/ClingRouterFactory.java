package client.cling;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.transport.RouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.AbstractRouterFactory;
import client.IRouter;



@SuppressWarnings("unused")
public class ClingRouterFactory extends AbstractRouterFactory {

    private static final long DISCOVERY_TIMEOUT_SECONDS = 5;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ClingRouterFactory() {
        super("Cling lib");
    }

    @Override
    protected List<IRouter> findRoutersInternal() throws RouterException {
    	//if PC use DefaultUpnpServiceConfiguration() 
    	final UpnpServiceConfiguration config = new DefaultUpnpServiceConfiguration();
    	//else if Android use AndroidUpnpServiceConfiguration().
        //final UpnpServiceConfiguration config = new AndroidUpnpServiceConfiguration();
        final ClingRegistryListener clingRegistryListener = new ClingRegistryListener();
        final UpnpService upnpService = new UpnpServiceImpl(config, clingRegistryListener);
        shutdownServiceOnExit(upnpService);

        log.debug("Start searching using upnp service");
        upnpService.getControlPoint().search();
        final RemoteService service = (RemoteService) clingRegistryListener
                .waitForServiceFound(DISCOVERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (service == null) {
            log.debug("Did not find a service after {} seconds", DISCOVERY_TIMEOUT_SECONDS);
            return Collections.emptyList();
        }

        log.debug("Found service {}", service);
        ClingRouter cRouter=new ClingRouter(service, upnpService.getRegistry(), upnpService.getControlPoint());
        return Arrays.<IRouter> asList(cRouter);
//        return Arrays
//                .<IRouter> asList(new ClingRouter(service, upnpService.getRegistry(), upnpService.getControlPoint()));
//		
		//return null;
    }

    

    private void shutdownServiceOnExit(UpnpService upnpService) {
		// TODO Auto-generated method stub
		
	}

	@Override
    protected IRouter connect(final String locationUrl) throws RouterException {
        throw new UnsupportedOperationException("Direct connection is not supported for Cling library.");
    }
}
