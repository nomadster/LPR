package server.keepalive;

import common.identifier.IdClient;
import server.data.DataServerInterface;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.TimerTask;
import common.remote.ClientRemote;

/**
 * Rappresenta il task che deve eliminare un client dalle strutture del
 * server quando non arriva il pacchetto di keep alive per un certo
 * tempo (il tempo è definito nella classe common.Configuration).
 * @author Federico Della Bona - Alessandro Lensi
 */
public class KillClient extends TimerTask {

    private IdClient id_da_uccidere;
    private GestoreKeepAlive gestore;
    private final DataServerInterface data;

    /**
     * Crea un Task
     * @param data DataServer in cui rimuovere il client id
     * @param gestore GestoreKeepAlive in cui rimuovere il client id
     * @param id client da eliminare
     */
    public KillClient(DataServerInterface data, GestoreKeepAlive gestore, IdClient id) {
        this.data = data;
        this.gestore = gestore;
        this.id_da_uccidere = id;
    }

    /**
     * Restituisce l'IdClient che questo KillClient deve eliminare allo
     * scadere del timer
     * @return l'IdClient che questo KillClient deve eliminare
     */
    public IdClient getId() {
        return id_da_uccidere;
    }

    /**
     * Restituisce il DataServer che questo KillClient deve aggiornare
     * @return il DataServer
     */
    public DataServerInterface getData() {
        return data;
    }

    /**
     * Restituisce il GestoreKeepAlive che questo KillClient deve aggiornare
     * @return il DataServer
     */
    public GestoreKeepAlive getGestore() {
        return gestore;
    }

    /**
     * Elimina il client dalle strutture del dataserver, dalle strutture
     * del gestore dei keep alive, e invoca anche un metodo remoto 
     * (removeClient) dei client per avvisarli dell'evento
     */
    @Override
    public void run() {

        System.err.println("Devo uccidere il client->" + id_da_uccidere);

        this.gestore.deleteTimer(this.id_da_uccidere);
        if (this.data.deleteClient(this.id_da_uccidere)) {
            synchronized (this.data) {
                Iterator i = this.data.scanCallback();
                while (i.hasNext()) {
                    ClientRemote cr = (ClientRemote) i.next();
                    try {
                        cr.removeClient(id_da_uccidere);
                    } catch (RemoteException ex) {
                        System.out.println("Eccezione removeClient remota:" + ex);
                    }
                }
            }
        }

    }
}
