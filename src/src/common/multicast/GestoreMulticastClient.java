package common.multicast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.List;
import common.identifier.IdClient;

/**
 * La classe GestoreMulticastClient estende la classe GestoreMulticast,
 * e ridefinisce il metodo da eseguire all'arrivo di ogni pacchetto.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class GestoreMulticastClient extends GestoreMulticast{

    private List listaClient;

    /**
     * Inizializza il GestoreMulticastClient
     * @param group indirizzo del gruppo a cui collegarsi
     * @param port porta su cui mettersi in ascolto
     * @param listaClient lista locale dei client da aggiornare
     * @throws IOException se c'è un errore nella creazione del socket
     */
    public GestoreMulticastClient(InetAddress group, int port,
            List listaClient) throws IOException{
        super(group, port);
        this.listaClient = listaClient;
    }


    /**
     * Il metodo close manda un pacchetto al gruppo multicast per
     * dichiarare la sua uscita dal gruppo
     * @param self l'IdClient inserito nel pacchetto
     * @throws IOException se c'è un errore di I/O nel lasciare il gruppo
     */
    public void close(IdClient self) throws IOException {
        if (self == null) {
            return;
        }
        DatagramPacket dp = null;

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buf);
        try {
            out.writeUTF(self.toString());
        } catch (IOException ex) {
            System.out.println("GESTORE MULTICAST CLIENT: "
                    + " errore nella spedizione del pacchetto");
        }
        System.out.println("GESTORE MULTICAST CLIENT: "
                    + " invio un pacchetto contenente "+ buf.toString());
        dp = new DatagramPacket(buf.toByteArray(), buf.size(), group, port);

        // ms.setTimeToLive(5);
        if (dp != null) {
            ms.send(dp);
        }
        this.shutdown = true;
        ms.leaveGroup(group);
        ms.close();

    }

    /**
     * Il metodo doSomething elimina il client c dalla lista locale
     * dei client.
     * @param c client da eliminare
     */
    @Override
    public void doSomething(IdClient c) {
        System.out.println("GESTORE MULTICAST CLIENT: "
                + "rimuovo il client " + c.toString());
        listaClient.remove(c);
    }
}
