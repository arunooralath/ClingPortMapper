package client.cling;

import org.fourthline.cling.model.message.control.IncomingActionResponseMessage;

public class ClingRouterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ClingRouterException(final String message, final IncomingActionResponseMessage response) {
        super(message);
    }

    public ClingRouterException(final String message) {
        super(message);
    }
}
