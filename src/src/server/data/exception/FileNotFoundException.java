package server.data.exception;

public class FileNotFoundException extends Exception{

    public FileNotFoundException(){}

    public FileNotFoundException(String messaggio){
        super(messaggio);
    }
}
