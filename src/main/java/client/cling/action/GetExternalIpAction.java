package client.cling.action;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.RemoteService;

public class GetExternalIpAction extends AbstractClingAction<String> {

    public GetExternalIpAction(final RemoteService service) {
        super(service, "GetExternalIPAddress");
    }

    public String convert(final ActionInvocation<RemoteService> invocation) {
        return (String) invocation.getOutput("NewExternalIPAddress").getValue();
    }
}
