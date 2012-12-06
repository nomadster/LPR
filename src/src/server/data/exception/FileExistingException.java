package server.data.exception;

public class FileExistingException extends Exception{

    public FileExistingException(){}

    public FileExistingException(String messaggio){
        super(messaggio);
    }

}
