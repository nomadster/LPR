package server.data.exception;

public class ClientNotActiveException extends Exception {

    public ClientNotActiveException(){}

    public ClientNotActiveException(String messaggio){
        super(messaggio);
    }

}
