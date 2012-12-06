package common.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import common.identifier.FileDescriptor;
import common.identifier.IdClient;

/**
 * Interfaccia remota del server
 * @author Federico Della Bona - Alessandro Lensi
 */
public interface ServerRemote extends Remote {

    /**
     * Metodo remoto tramite il quale i client si registrano nel sistema
     * @param client IdClient del client che vuole registrarsi
     * @param callback Callback remota del client che vuole registrarsi
     * @return la lista di IdClient registrati in quel momento nel server
     * @throws ClientAlreadyRegisteredException Se il client risulta
     * già presente nella lista dei client registrati.
     *
     */
    public List<IdClient> register(IdClient client, ClientRemote callback) throws RemoteException;

    /**
     * Nel caso in cui il file non esista lo inserisce tra quelli pubblicati
     * con il client che lo ha pubblicato come primo seeder
     * @param client Il client che vuole pubblicare il file
     * @param file Il file da pubblicare
     * @return Restituisce false se è già stato pubblicato un file
     * con lo stesso nome, true altrimenti
     * @throws RemoteException Nel caso in cui il client risulti non registrato
     */
    public boolean publish(IdClient client, FileDescriptor file) throws RemoteException;

    /**
     * Se esiste un seeder per fileName, il server inserisce il client
     * che ha fatto la richiesta tra i leachers del file.
     * @param client Il client che ha fatto la richiesta
     * @param fileName Il file da cercare
     * @return Restituisce null se il file non * pubblicato altrimenti
     * restituisce una struttura contenente l’identificatore di un seeder
     * del file e il descrittore del file.
     * @throws RemoteException Nel caso in cui il client risulti non registrato
     */
    public SearchResult search(IdClient client, String fileName) throws RemoteException;

    /**
     * Segnala al server che il client ha finito di scaricare il file.
     * Il server lo sposta dalla lista dei leachers a quella dei seeders.
     * @param client Il Client che ha fatto la richiesta
     * @param file Il file completato
     * @throws RemoteException Nel caso in cui ci siano problemi con il client
     * e/o con il file
     */
    public void completed(IdClient client, FileDescriptor file) throws RemoteException;

    /**
     * Segnala al server che il client ha avuto problemi nello scaricare
     * il file così lo può rimuovere dalla lista dei leachers
     * @param filename Il file da rimuovere
     * @param client Il client che ha fatto la richiesta
     * @throws RemoteException Nessuna eccezione viene sollevata
     */
    public void transferFailed(String filename, IdClient client) throws RemoteException;
}
