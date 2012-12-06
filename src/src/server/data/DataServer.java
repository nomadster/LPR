package server.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import server.data.exception.ClientNotActiveException;
import server.data.util.FileCondiviso;
import common.identifier.FileDescriptor;
import server.data.exception.FileExistingException;
import server.data.exception.FileNotFoundException;
import common.identifier.IdClient;
import server.data.exception.LeacherNotFoundException;
import server.data.exception.SeederExistsException;
import server.data.util.TableClientAttivi;
import server.data.util.TableClientInterface;
import server.data.util.TableFileCondivisi;
import server.data.util.TableFileInterface;
import common.remote.ClientRemote;

/**
 * La classe realizza un piccolo database, che mantiene come informazioni,
 * i client attivi, i file condivisi, le callback dei client, i seeders e
 * i leachers dei file condivisi.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class DataServer implements DataServerInterface {

    private TableClientInterface tab_client;
    private TableFileInterface tab_file;

    /**
     * crea una DataServer vuoto
     */
    public DataServer() {
        tab_client = new TableClientAttivi();
        tab_file = new TableFileCondivisi();
    }

    /**
     * Aggiunge un client al database
     * @param c client da aggiungere
     * @param callback interfaccia remota del client
     * @return true se l'inserimento è andato a buon fine (non c'era già);
     *         false altrimenti.
     */
    public synchronized boolean insertClient(
            IdClient c, ClientRemote callback) {
        return tab_client.addClient(c, callback);
    }

    /**
     * Elimina il client c dal database
     * @param c client da rimuovere
     * @return true se il c era nel database; false altrimenti.
     */
    public synchronized boolean deleteClient(IdClient c) {
        if (!this.isActive(c)) {
            return false;
        }
        Iterator posseduti = tab_client.scanFilePosseduti(c);
        while (posseduti.hasNext()) {
            try {
                tab_file.removeSeeder((FileDescriptor) posseduti.next(), c);
            } catch (server.data.exception.FileNotFoundException ex) {
                System.out.print("deleteClient -> removeSeeder: " + ex);
            }
        }
        Iterator richiesti = tab_client.scanFileRichiesti(c);
        while (richiesti.hasNext()) {
            try {
                tab_file.removeLeacher((FileDescriptor) richiesti.next(), c);
            } catch (server.data.exception.FileNotFoundException ex) {
                System.out.print("deleteClient -> removeLeacher: " + ex);
            }
        }
        return tab_client.removeClient(c);

    }

    /**
     * Restituisce la Lista dei client attivi
     * @return la Lista dei client attivi
     */
    public synchronized List<IdClient> getClientList() {
        return tab_client.toList();
    }

    /**
     * Aggiunge il file f al database e mette il client c come primo ed unico
     * seeder del file
     * @param c il client da aggiungere come seeder ad f
     * @param f il file da aggiungere al database
     * @return true se il file non c'era; false altrimenti
     * @throws ClientNotActiveException se il client c non è attivo
     */
    public synchronized boolean insertFile(IdClient c, FileDescriptor f)
            throws ClientNotActiveException {
        boolean ret;
        if (!tab_client.checkClient(c)) {
            throw new ClientNotActiveException();
        }
        if (ret = tab_file.addFile(f, c)) {
            tab_client.addFilePosseduto(c, f);
            try {
                tab_file.addSeeder(f, c);
            } catch (server.data.exception.FileNotFoundException ex) {
                System.out.print("InsertFile -> addSeeder: " + ex);
            }
        }
        return ret;
    }

    /**
     * Se esiste il file filename nel database, restituisce il FileDescriptor
     * ed aggiunge c come leacher di questo.
     * @param FileName il nome del file di cui si vuole il FileDescriptor
     * @return FileDescriptor se il file c'è, null altrimenti.
     */
    public synchronized FileDescriptor getFile(String FileName, IdClient c)
            throws ClientNotActiveException {
        if (!tab_client.checkClient(c)) {
            throw new ClientNotActiveException();
        }
        FileDescriptor ret = tab_file.getFileDescriptor(FileName);


        if (ret == null) {
            return null;
        }

        if (!tab_client.addFileRichiesto(c, ret)) {
            /* sei già un Leacher o Seeder per quel file */
            return null;
        }

        try {
            if (!tab_file.addLeacher(ret, c)) {
                /* sei già un Leacher o Seeder per quel file */
                return null;
            }
        } catch (server.data.exception.FileNotFoundException ex) {
            System.err.print("getFile -> addLeacher: " + ex);
        }

        return ret;
    }

    /**
     * Restituisce un seeder per il file f
     * @param f il file di cui si vuole ottenere un seeder
     * @return null se non c'è nessun seeder; il seeder altrimenti
     */
    public synchronized IdClient getSeeder(FileDescriptor f) {
        return tab_file.getSeeder(f);
    }

    /**
     * Se il client c è un leacher del file f, questo diventa un seeder di f
     * @param c il client da cambiare
     * @param f il file di cui c è leacher
     * @throws FileNotFoundException se f non è nella tabella dei files
     * @throws ClientNotActiveException se c non è un client attivo
     * @throws SeederExistsException se c è già seeder di f
     * @throws LeacherNotFoundException se c non era leacher di f
     */
    public synchronized void leacherToSeeder(IdClient c, FileDescriptor f)
            throws FileNotFoundException, ClientNotActiveException,
            SeederExistsException, LeacherNotFoundException {

        if (!tab_client.checkClient(c)) {
            throw new ClientNotActiveException();
        }

        tab_file.leacherToSeeder(f, c);

        try {
            tab_client.moveFileNeiCompletati(c, f);
        } catch (FileExistingException ex) {
            throw new SeederExistsException();
        }
    }

    /**
     * Controlla se il client c è attivo
     * @param c client da controllare
     * @return true se c è attivo, false altrimenti
     */
    public synchronized boolean isActive(IdClient c) {
        return this.tab_client.checkClient(c);
    }

    /**
     * Stampa a video la tabella dei file e la tabella dei client
     */
    public synchronized void printDataServer() {
        List<IdClient> l = this.tab_client.toList();
        Collection c = this.tab_file.toCollection();
        Iterator scanFile = c.iterator();
        Iterator i = l.iterator();

        System.out.println("*-*-*-*----Tabella dei Client----*-*-*-*");
        while (i.hasNext()) {
            IdClient client = (IdClient) i.next();
            System.out.println(client.toString());
            this.tab_client.printClient(client);
            System.out.println("---------------------------------------");
        }
        System.out.println("*-*-*-*----Tabella dei File----*-*-*-*");
        while (scanFile.hasNext()) {
            FileCondiviso f = (FileCondiviso) scanFile.next();
            System.out.println(f.getFileDescriptor().getName());
            this.tab_file.printFile(f.getFileDescriptor());
            System.out.println("---------------------------------------");
        }
    }






    /**
     * Restituisce un iteratore per le callback dei client attivi
     * @return un iteratore per le callback dei client attivi
     */
    public Iterator scanCallback() {
        return tab_client.scanCallback();
    }

    /**
     * Rimuove client come leacher del file filename
     * @param filename file da aggiornare
     * @param client da rimuovere come leacher del file filename
     */
    public synchronized void removeLeacher(String filename, IdClient client) {
        if (client == null) {
            return;
        }
        FileDescriptor fd = this.tab_file.getFileDescriptor(filename);
        if (fd == null) {
            return;
        }
        if (this.tab_client.removeFileRichiesto(client, fd)) {
            try {
                this.tab_file.removeLeacher(fd, client);
            } catch (Exception e) {
                return;
            }
        }
        return;
    }
}
