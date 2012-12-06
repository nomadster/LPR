package common.remote.exception;

import java.rmi.RemoteException;

public class InvalidRemoteArgumentException extends RemoteException {
    public InvalidRemoteArgumentException() {
        super();
    }
    public InvalidRemoteArgumentException(String msg) {
        super(msg);
    }

}
