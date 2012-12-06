package common.remote.exception;

import java.rmi.RemoteException;

public class ClientAlreadyRegisteredException extends RemoteException{

    public ClientAlreadyRegisteredException() {
        super();
    }

    public ClientAlreadyRegisteredException(String msg){
        super(msg);
    }
}
