package common.remote.exception;

import java.rmi.RemoteException;

public class ClientNotActiveException extends RemoteException {
    public ClientNotActiveException() {
        super();
    }

    public ClientNotActiveException(String msg){
        super(msg);
    }

}
