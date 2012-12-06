package server.data.exception;

public class SeederExistsException extends Exception {

    public SeederExistsException(){}

    public SeederExistsException(String messaggio){
        super(messaggio);
    }

}
