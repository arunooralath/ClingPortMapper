
package client.cling.action;

import java.util.HashMap;
import java.util.Map;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

import client.PortMapping;

public class DeletePortMappingAction extends AbstractClingAction<Void> {

    private final int externalPort;
    private final String protocol;
    private final String remoteHost;

    public DeletePortMappingAction(final RemoteService service, final PortMapping portMapping) {
        super(service, "DeletePortMapping");
        this.externalPort = portMapping.getExternalPort();
        this.protocol = portMapping.getProtocol().getName();
        this.remoteHost = portMapping.getRemoteHost();
    }

    @Override
    public Map<String, Object> getArgumentValues() {
        final HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("NewExternalPort", new UnsignedIntegerTwoBytes(externalPort));
        args.put("NewProtocol", protocol);
        args.put("NewRemoteHost", remoteHost);
        return args;
    }

    public Void convert(final ActionInvocation<RemoteService> response) {
        return null;
    }
}
