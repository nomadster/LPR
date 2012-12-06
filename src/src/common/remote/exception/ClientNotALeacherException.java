package common.remote.exception;

import java.rmi.RemoteException;

public class ClientNotALeacherException extends RemoteException {

    public ClientNotALeacherException() {
        super();
    }

    public ClientNotALeacherException(
            String msg) {
        super(msg);

    }
}
