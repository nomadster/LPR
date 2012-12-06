package common.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import common.identifier.IdClient;

/**
 * Interfaccia remota del client.
 * @author Federico Della Bona - Alessandro Lensi
 */
public interface ClientRemote extends Remote {

    /**
     * Aggiunge l’identificatore del client passato per
     * argomento alla lista di client attivi.
     * @param c Il client da aggiungere
     * @throws RemoteException Non la lancia
     */
    void addClient(IdClient c) throws RemoteException;

    /**
     * Rimuove l’identificatore del client passato per
     * argomento dalla lista di client attivi.
     * @param c Il client da rimuovere
     * @throws RemoteException Non la lancia
     */
    void removeClient(IdClient c) throws RemoteException;

    /**
     * Metodo remoto con il quale il server notifica al client
     * la propria imminente terminazione
     * @throws RemoteException Non la lancia
     */
    void serverShuttingDown() throws RemoteException;
}
