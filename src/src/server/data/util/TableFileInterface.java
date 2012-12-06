package server.data.util;

import java.util.Collection;
import server.data.exception.ClientNotActiveException;
import common.identifier.FileDescriptor;
import server.data.exception.FileNotFoundException;
import common.identifier.IdClient;
import server.data.exception.SeederExistsException;

/**
 * L'interfaccia TableFileInterface può essere implementata da una classe
 * che realizza una tabella di FileDescriptor, che mantiene come
 * informazioni, la lista dei seeders e la lista dei leachers per file
 * associato al FileDescriptor.
 * @author Federico Della Bona - Alessandro Lensi
 */
public interface TableFileInterface {


    /**
     * Aggiunge il file f alla tabella, con seeder l'idClient passato
     * come argomento
     * @param f file da aggiungere
     * @param seeder del file f
     * @return true se l'inserimento ha successo,
     *         false se il file f è già nella tabella
     */
    public boolean addFile(FileDescriptor f, IdClient seeder);


    /**
     * Controlla se il file è già nella tabella
     * @param f file da controllare
     * @return true se il file è nella tabella,
     *         false altrimenti
     */
    public boolean checkFile(FileDescriptor f);


    /**
     * Restituisce il FileDescriptor relativo al file che si chiama filename
     * @param filename nome del file
     * @return il FileDescriptor associato a filename
     */
    public FileDescriptor getFileDescriptor(String fileName);

    /**
     * Aggiunge il client id ai seeders di f
     * @param f file da aggiornare
     * @param id client da aggiungere
     * @return true se il client è stato aggiunto correttamente,
     *         false se era già tra i seeders o leachers di f
     * @throws FileNotFoundException se f non è nella tabella
     */
    public boolean addSeeder(FileDescriptor f, IdClient id) throws FileNotFoundException;


    /**
     * Aggiunge il client id alla lista dei leachers di f
     * @param id client da aggiungere
     * @param f file da aggiornare
     * @return true se l'inserimento ha successo,
     *         false se id è già nei seeders o nei leachers di f
     * @throws FileNotFoundException se f non è nella tabella
     */
    public boolean addLeacher(FileDescriptor f, IdClient id) throws FileNotFoundException;


    /**
     * Rimuove il client id dalla lista dei leachers
     * e lo inserisce nella lista dei seeders del file f
     * @param id client da spostare
     * @param f file da aggiornare
     * @throws FileNotFoundException se f non è nella tabella
     * @throws ClientNotActiveException se id non è leacher di f
     * @throws SeederExistsException se id è già seeder di f
     */
    public void leacherToSeeder(FileDescriptor f, IdClient id)
            throws FileNotFoundException,ClientNotActiveException,SeederExistsException;


    /**
     * Rimuove il client id dalla lista dei seeders di f, se era l'unico
     * seeder rimuove anche il file dalla tabella
     * @param f file da aggiornare
     * @param id client da rimuovere
     * @return true se la cancellazione ha successo,
     *         false se id non è seeder di f
     * @throws FileNotFoundException se f non è nella tabella
     */
    public boolean removeSeeder(FileDescriptor f, IdClient id) throws FileNotFoundException;


    /**
     * Rimuove il client id dalla lista dei leachers di f
     * @param f file da aggiornare
     * @param id client da rimuovere
     * @return true se la cancellazione ha successo,
     *         false se id non è leacher di f
     * @throws FileNotFoundException se f non è nella tabella
     */
    public boolean removeLeacher(FileDescriptor f, IdClient id) throws FileNotFoundException;


    /**
     * Restituisce un seeder per il file f
     * @param f file
     * @return un seeder per f
     */
    public IdClient getSeeder(FileDescriptor f);


    /**
     * Stampa le informazioni del file f
     * @param f file
     */
    public void printFile(FileDescriptor f);


    /**
     * Restituisce una collezione degli elementi
     * @return la collezione degli elementi
     */
    public Collection toCollection(); //serve solo per la printDataServer

}
