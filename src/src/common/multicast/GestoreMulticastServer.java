package common.multicast;

import server.data.DataServerInterface;
import java.io.IOException;
import java.net.InetAddress;
import common.identifier.IdClient;

/**
 * La classe GestoreMulticastServer estende la classe GestoreMulticast
 * e ridefinisce l'azione da compiere all'arrivo di ogni pacchetto.
 * (metodo doSomething)
 * @author Federico Della Bona - Alessandro Lensi
 */
public class GestoreMulticastServer extends GestoreMulticast{

    private DataServerInterface data;


    /**
     * Inizializza il GestoreMulticastClient
     * @param group indirizzo del gruppo a cui collegarsi
     * @param port porta su cui mettersi in ascolto
     * @param data DataServer da aggiornare
     * @throws IOException se c'è un errore nella creazione del socket
     */
    public GestoreMulticastServer(InetAddress group, int port,
            DataServerInterface data) throws IOException{
        super(group, port);
        this.data = data;
    }

    /**
     * Il metodo doSomething elimina il client c dal DataServer
     * @param c client da eliminare
     */
    @Override
    public void doSomething(IdClient c) {
        System.out.println("GESTORE MULTICAST SERVER: "
                + "rimuovo il client " + c.toString());
        this.data.deleteClient(c);
    }

}
