package server.data.util;

import common.remote.ClientRemote;


import java.util.Iterator;
import java.util.List;
import server.data.exception.ClientNotActiveException;
import common.identifier.FileDescriptor;
import server.data.exception.FileExistingException;
import server.data.exception.FileNotFoundException;
import common.identifier.IdClient;


/**
 * L'interfaccia TableClientInterface è implementata da una classe che
 * realizza una tabella di client, che mantiene come informazioni
 * la lista dei file posseduti, la lista dei file richiesti, e la callback
 * del client.
 * @author Federico Della Bona - Alessandro Lensi
 */
public interface TableClientInterface {

    /**
     * Aggiunge un nuovo client con relativa callback nella tabella
     * @param id identificatore del cliente
     * @param callback callback del client
     * @return true se l'inserimento ha successo,
     *         false se il client è già nella tabella
     */
    public boolean addClient(IdClient id, ClientRemote callback);


    /**
     * Rimuove il client dalla tabella
     * @param id identificatore del cliente
     * @return true se la cancellazione ha successo,
     *         false se il client non è presente nella tabella
     */
    public boolean removeClient(IdClient id);


    /**
     * Controlla se il client è già nella tabella
     * @param id identificatore del cliente
     * @return true se il client è nella tabella,
     *         false altrimenti
     */
    public boolean checkClient(IdClient id);



    /**
     * Aggiunge il file f alla lista dei file richiesti di id
     * @param id identificatore del cliente
     * @param f file da aggiungere
     * @return true se f viene inserito,
     *         false se f era già presente tra i file posseduti o richiesti
     * @throws ClientNotActiveException se il client non è presente nella tabella
     */
    public boolean addFileRichiesto(IdClient id, FileDescriptor f)
            throws ClientNotActiveException;

    /**
     * Rimuove file dalla lista dei file richiesti di id
     * @param id identificatore del cliente
     * @param file file da rimuovere
     * @return true se il file è contenuto nella lista dei file richiesti,
     *         false altrimenti
     */
     public boolean removeFileRichiesto(IdClient id, FileDescriptor file);

    /**
     * Aggiunge il file f alla lista dei file posseduti di id
     * @param id identificatore del cliente
     * @param f file da aggiungere
     * @return true se f viene inserito,
     *         false se f era già presente tra i file posseduti o richiesti
     * @throws ClientNotActiveException
     */
    public boolean addFilePosseduto(IdClient id, FileDescriptor f)
            throws ClientNotActiveException;

    /**
     * Rimuove il file f dalla lista dei file richiesti
     * e lo inserisci nella lista dei file posseduti di id.
     * @param id identificatore del cliente
     * @param f file da spostare
     * @throws ClientNotActiveException se id non è presente nella tabela
     * @throws FileNotFoundException se f non è tra i file richiesti
     * @throws FileExistingException se f è già tra i file posseduti
     */
    public void moveFileNeiCompletati(IdClient id, FileDescriptor f)
            throws ClientNotActiveException, FileNotFoundException, FileExistingException;
    
    
    /**
     * Restituisce una lista di idClient
     * @return una lista di identificatori di cliente
     */
    public List<IdClient> toList();


    /**
     * Restituisce un generatore di callback
     * @return iterator di callback di tipo ClientRemote
     */
    public Iterator scanCallback();


    /**
     * Restituisce un generatore dei file posseduti da id
     * @param id identificatore del cliente
     * @return iterator di FileDescriptor
     */
    public Iterator scanFilePosseduti(IdClient id);


    /**
     * Restituisce un generatore dei file richiesti da id
     * @param id identificatore del cliente
     * @return iterator di FileDescriptor
     */
    public Iterator scanFileRichiesti(IdClient id);

    
    /**
     * Stampa le informazioni del client id
     * @param id identificatore del cliente
     */
    public void printClient(IdClient id);

   
}
