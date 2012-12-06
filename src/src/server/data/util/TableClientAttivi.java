package server.data.util;

import common.remote.ClientRemote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import server.data.exception.ClientNotActiveException;
import common.identifier.FileDescriptor;
import server.data.exception.FileExistingException;
import server.data.exception.FileNotFoundException;
import common.identifier.IdClient;

/**
 * La classe TableClientAttivi realizza una tabella di client, che mantiene
 * come informazioni la lista dei file posseduti, la lista dei file richiesti,
 * e la callback del client:
 * @author Federico Della Bona - Alessandro Lensi
 */
public class TableClientAttivi implements TableClientInterface {

    private HashMap table;
    private List<IdClient> lista;
    private List<ClientRemote> callback;

    /**
     * Crea una tabella vuota
     */
    public TableClientAttivi() {
        this.table = new HashMap();
        this.lista = new LinkedList<IdClient>();
        this.callback = new LinkedList<ClientRemote>();
    }

    /**
     * Aggiunge un nuovo client con relativa callback nella tabella
     * @param id identificatore del cliente
     * @param callback callback del client
     * @return true se l'inserimento ha successo,
     *         false se il client è già nella tabella
     */
    public boolean addClient(IdClient id, ClientRemote callback) {

        if (this.table.get(id) == null) {
            this.table.put(id, new ClientAttivo());
            this.lista.add(id);
            this.callback.add(callback);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Controlla se il client è già nella tabella
     * @param id identificatore del cliente
     * @return true se il client è nella tabella,
     *         false altrimenti
     */
    public boolean checkClient(IdClient id) {
        return this.table.get(id) != null;
    }

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
            throws ClientNotActiveException, FileNotFoundException,
            FileExistingException {

        ClientAttivo ca = (ClientAttivo) this.table.get(id);

        if (ca == null) {
            throw new ClientNotActiveException();
        }

        this.printClient(id);
        
        boolean pp = ca.removeFileRichiesto(f);


         if (pp) {

             boolean rara = ca.containsNeiPosseduti(f);
            if (rara) {
                throw new FileExistingException();
            } else {
                ca.addFilePosseduto(f);
            }
        } else {
            throw new FileNotFoundException();
        }
    }

    /**
     * Aggiunge il file f alla lista dei file richiesti di id
     * @param id identificatore del cliente
     * @param f file da aggiungere
     * @return true se f viene inserito,
     *         false se f era già presente tra i file posseduti o richiesti
     * @throws ClientNotActiveException se il client non è presente nella
     * tabella
     */
    public boolean addFileRichiesto(IdClient id, FileDescriptor f)
            throws ClientNotActiveException {

        ClientAttivo ca = (ClientAttivo) this.table.get(id);
        if (ca == null) {
            throw new ClientNotActiveException();
        }
        if (ca.containsNeiPosseduti(f) || ca.containsNeiRichiesti(f)) {
            return false;
        }
        ca.addFileRichiesto(f);
        return true;
    }

    /**
     * Stampa le informazioni del client id
     * @param id identificatore del cliente
     */
    public void printClient(IdClient id) {
        ClientAttivo ca = (ClientAttivo) this.table.get(id);
        ca.stampa();
    }

    /**
     * Rimuove il client dalla tabella
     * @param id identificatore del cliente
     * @return true se la cancellazione ha successo,
     *         false se il client non è presente nella tabella
     */
    public boolean removeClient(IdClient id) {
        int index = 0;
        if (checkClient(id)) {
            this.table.remove(id);
            index = this.lista.indexOf(id);
            this.lista.remove(id);
            this.callback.remove(index);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Aggiunge il file f alla lista dei file posseduti di id
     * @param id identificatore del cliente
     * @param f file da aggiungere
     * @return true se f viene inserito,
     *         false se f era già presente tra i file posseduti o richiesti
     * @throws ClientNotActiveException
     */
    public boolean addFilePosseduto(IdClient id, FileDescriptor f)
            throws ClientNotActiveException {
        ClientAttivo ca = (ClientAttivo) this.table.get(id);
        if (ca == null) {
            throw new ClientNotActiveException();
        }
        if (ca.containsNeiPosseduti(f) || ca.containsNeiRichiesti(f)) {
            return false;
        }
        ca.addFilePosseduto(f);
        return true;
    }

    /**
     * Restituisce una lista di idClient
     * @return una lista di identificatori di cliente
     */
    public List<IdClient> toList() {
        return lista;
    }

    /**
     * Restituisce un generatore di callback
     * @return iterator di callback di tipo ClientRemote
     */
    public Iterator scanCallback() {
        return this.callback.iterator();
    }

    /**
     * Restituisce un generatore dei file posseduti da id
     * @param id identificatore del cliente
     * @return iterator di FileDescriptor
     */
    public Iterator scanFilePosseduti(IdClient id) {
        ClientAttivo ca = (ClientAttivo) this.table.get(id);
        return ca.scanPosseduti();
    }

    /**
     * Restituisce un generatore dei file richiesti da id
     * @param id identificatore del cliente
     * @return iterator di FileDescriptor
     */
    public Iterator scanFileRichiesti(IdClient id) {
        ClientAttivo ca = (ClientAttivo) this.table.get(id);
        return ca.scanRichiesti();
    }

    /**
     * Rimuove file dalla lista dei file richiesti di id
     * @param id identificatore del cliente
     * @param file file da rimuovere
     * @return true se il file è contenuto nella lista dei file richiesti,
     *         false altrimenti
     */
    public boolean removeFileRichiesto(IdClient id, FileDescriptor file) {
        ClientAttivo cc = (ClientAttivo) table.get(id);
        if (cc == null) {
            return false;
        }
        return cc.removeFileRichiesto(file);
    }
}
