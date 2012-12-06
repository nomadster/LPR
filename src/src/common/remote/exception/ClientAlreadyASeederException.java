package common.remote.exception;

import java.rmi.RemoteException;

public class ClientAlreadyASeederException extends RemoteException{

    public ClientAlreadyASeederException() {
        super();
    }

    public ClientAlreadyASeederException(String msg) {
        super(msg);
    }



}
