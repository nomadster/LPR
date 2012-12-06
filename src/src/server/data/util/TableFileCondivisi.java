package server.data.util;

import java.util.Collection;
import java.util.HashMap;
import server.data.exception.ClientNotActiveException;
import common.identifier.FileDescriptor;
import server.data.exception.FileNotFoundException;
import common.identifier.IdClient;
import server.data.exception.SeederExistsException;

/**
 * La classe TableFileCondivisi realizza una tabella di FileDescriptor,
 * che mantiene come informazioni, la lista dei seeders e la lista dei
 * leachers per file associato al FileDescriptor.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class TableFileCondivisi implements TableFileInterface {

    private HashMap table;

    /**
     * Crea una tabella vuota
     */
    public TableFileCondivisi() {
        table = new HashMap();
    }

    /**
     * Aggiunge il file f alla tabella, con seeder l'idClient passato
     * come argomento
     * @param f file da aggiungere
     * @param seeder del file f
     * @return true se l'inserimento ha successo,
     *         false se il file f è già nella tabella
     */
    public boolean addFile(FileDescriptor f, IdClient seeder) {
        FileCondiviso fc;
        if (this.table.get(f.getName()) == null) {
            fc = new FileCondiviso(f);
            fc.addSeeder(seeder);
            this.table.put(f.getName(), fc);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Controlla se il file è già nella tabella
     * @param f file da controllare
     * @return true se il file è nella tabella,
     *         false altrimenti
     */
    public boolean checkFile(FileDescriptor f) {
        return this.table.get(f.getName()) != null;
    }

    /**
     * Restituisce il FileDescriptor relativo al file che si chiama filename
     * @param filename nome del file
     * @return il FileDescriptor associato a filename
     */
    public FileDescriptor getFileDescriptor(String fileName) {
        FileCondiviso fc = (FileCondiviso) this.table.get(fileName);
        if (fc == null) {
            return null;
        }
        return fc.getFileDescriptor();
    }

    /**
     * Aggiunge il client id ai seeders di f
     * @param f file da aggiornare
     * @param id client da aggiungere
     * @return true se il client è stato aggiunto correttamente,
     *         false se era già tra i seeders o leachers di f
     * @throws FileNotFoundException se f non è nella tabella
     */
    public boolean addSeeder(FileDescriptor f, IdClient id) throws FileNotFoundException {
        FileCondiviso fc = (FileCondiviso) this.table.get(f.getName());
        if (fc == null) {
            throw new FileNotFoundException();
        }
        if (fc.containsSeeder(id) || fc.containsLeacher(id)) {
            return false;
        }
        fc.addSeeder(id);
        return true;
    }

    /**
     * Aggiunge il client id alla lista dei leachers di f
     * @param id client da aggiungere
     * @param f file da aggiornare
     * @return true se l'inserimento ha successo,
     *         false se id è già nei seeders o nei leachers di f
     * @throws FileNotFoundException se f non è nella tabella
     */
    public boolean addLeacher(FileDescriptor f, IdClient id) throws FileNotFoundException {
        FileCondiviso fc = (FileCondiviso) this.table.get(f.getName());
        if (fc == null) {
            throw new FileNotFoundException();
        }
        if (fc.containsSeeder(id) || fc.containsLeacher(id)) {
            return false;
        }
        fc.addLeacher(id);
        return true;
    }

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
            throws FileNotFoundException, ClientNotActiveException,
            SeederExistsException {
        FileCondiviso fc = (FileCondiviso) this.table.get(f.getName());
        if (fc == null) {
            throw new FileNotFoundException();
        } else if (fc.removeLeacher(id)) {
            if (fc.containsSeeder(id)) {
                throw new SeederExistsException();
            } else {
                fc.addSeeder(id);
            }
        } else {
            throw new ClientNotActiveException();
        }
    }

    /**
     * Rimuove il client id dalla lista dei seeders di f, se era l'unico
     * seeder rimuove anche il file dalla tabella
     * @param f file da aggiornare
     * @param id client da rimuovere
     * @return true se la cancellazione ha successo,
     *         false se id non è seeder di f
     * @throws FileNotFoundException se f non è nella tabella
     */
    public boolean removeSeeder(FileDescriptor f, IdClient id)
            throws FileNotFoundException {
        FileCondiviso fc = (FileCondiviso) this.table.get(f.getName());
        if (fc == null) {
            throw new FileNotFoundException();
        }
        if (fc.removeSeeder(id)) {
            if (fc.sizeSeeder() == 0 && fc.sizeLeacher() == 0) {//rimuove il file se tutte le liste son vuote
                this.table.remove(f.getName());
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Rimuove il client id dalla lista dei leachers di f
     * @param f file da aggiornare
     * @param id client da rimuovere
     * @return true se la cancellazione ha successo,
     *         false se id non è leacher di f
     * @throws FileNotFoundException se f non è nella tabella
     */
    public boolean removeLeacher(FileDescriptor f, IdClient id)
            throws FileNotFoundException {
        FileCondiviso fc = (FileCondiviso) this.table.get(f.getName());
        if (fc == null) {
            throw new FileNotFoundException();
        }
        if (fc.removeLeacher(id)) {
            if (fc.sizeLeacher() == 0 && fc.sizeSeeder() == 0) {
                this.table.remove(f.getName());
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Restituisce un seeder per il file f
     * @param f file
     * @return un seeder per f
     */
    public IdClient getSeeder(FileDescriptor f) {
        FileCondiviso fc = (FileCondiviso) this.table.get(f.getName());
        if (fc != null) {
            return fc.getSeeder();
        }
        return null;
    }

    /**
     * Stampa le informazioni del file f
     * @param f file
     */
    public void printFile(FileDescriptor f) {
        FileCondiviso fc = (FileCondiviso) this.table.get(f.getName());
        fc.stampa();
    }

    /**
     * Restituisce una collezione degli elementi
     * @return la collezione degli elementi
     */
    public Collection toCollection() { //serve solo per la printDataServer
        return this.table.values();
    }
}
