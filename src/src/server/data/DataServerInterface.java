package server.data;

import java.util.Iterator;
import java.util.List;
import server.data.exception.SeederExistsException;
import server.data.exception.ClientNotActiveException;
import common.identifier.FileDescriptor;
import server.data.exception.FileNotFoundException;
import common.identifier.IdClient;
import server.data.exception.LeacherNotFoundException;
import common.remote.ClientRemote;

/**
 * L'interfaccia DataServerInterface può essere implementata da una classe
 * che realizza un database, che mantiene come informazioni, i client attivi,
 * i file condivisi, le callback dei client, i seeders e i leachers dei file
 * condivisi.
 * @author Federico Della Bona - Alessandro Lensi
 */
public interface DataServerInterface {

    /**
     * Aggiunge un client al database
     * @param c client da aggiungere
     * @param callback interfaccia remota del client
     * @return true se l'inserimento è andato a buon fine (non c'era già); 
     *         false altrimenti.
     */
    public boolean insertClient(IdClient c, ClientRemote callback);

    /**
     * Elimina il client c dal database
     * @param c client da rimuovere
     * @return true se il c era nel database; false altrimenti.
     */
    public boolean deleteClient(IdClient c);


    /**
     * Restituisce la Lista dei client attivi
     * @return la Lista dei client attivi
     */
    public List<IdClient> getClientList();


    /**
     * Aggiunge il file f al database e mette il client c come primo ed unico
     * seeder del file
     * @param c il client da aggiungere come seeder ad f
     * @param f il file da aggiungere al database
     * @return true se il file non c'era; false altrimenti
     * @throws ClientNotActiveException se il client c non è attivo
     */
    public boolean insertFile(IdClient c, FileDescriptor f)
            throws ClientNotActiveException;

    /**
     * Se esiste il file filename nel database, restituisce il FileDescriptor
     * ed aggiunge c come leacher di questo.
     * @param FileName il nome del file di cui si vuole il FileDescriptor
     * @return FileDescriptor se il file c'è, null altrimenti.
     */
    public FileDescriptor getFile(String FileName, IdClient c)
            throws ClientNotActiveException;

    /**
     * Restituisce un seeder per il file f
     * @param f il file di cui si vuole ottenere un seeder
     * @return null se non c'è nessun seeder; il seeder altrimenti
     */
    public IdClient getSeeder(FileDescriptor f);


    /**
     * Se il client c è un leacher del file f, questo diventa un seeder di f
     * @param c il client da cambiare
     * @param f il file di cui c è leacher
     * @throws FileNotFoundException se f non è nella tabella dei files
     * @throws ClientNotActiveException se c non è un client attivo
     * @throws SeederExistsException se c è già seeder di f
     * @throws LeacherNotFoundException se c non era leacher di f
     */
    public void leacherToSeeder(IdClient c, FileDescriptor f)
            throws FileNotFoundException,
            ClientNotActiveException,
            SeederExistsException,
            LeacherNotFoundException;


    /**
     * Controlla se il client c è attivo
     * @param c client da controllare
     * @return true se c è attivo, false altrimenti
     */
    public boolean isActive(IdClient c);

    /**
     * Restituisce un iteratore per le callback dei client attivi
     * @return un iteratore per le callback dei client attivi
     */
    public Iterator scanCallback();

    /**
     * Stampa a video la tabella dei file e la tabella dei client
     */
    public void printDataServer();
    /**
     * Rimuove client come leacher del file filename
     * @param filename file da aggiornare
     * @param client da rimuovere come leacher del file filename
     */
    public void removeLeacher(String filename, IdClient client);
}
