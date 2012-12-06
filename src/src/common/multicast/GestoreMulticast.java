package common.multicast;

import common.Configuration;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import common.identifier.IdClient;

/**
 * La classe astratta GestoreMulticast serve per collegarsi e
 * mettersi in ascolto su un gruppo multicast. Le classi che estendono la
 * classe GestoreMulticast devono specializzare l'azione da compiere
 * all'arrivo di un pacchetto. (metodo doSomething)
 * @author Federico Della Bona - Alessandro Lensi
 */
public abstract class GestoreMulticast implements Runnable {

    InetAddress group;
    int port;
    MulticastSocket ms;
    boolean shutdown;

    /**
     * Inizializza il GestoreMulticast
     * @param group indirizzo del gruppo a cui collegarsi
     * @param port porta su cui mettersi in ascolto
     * @throws IOException se c'è un errore nella creazione del socket
     */
    public GestoreMulticast(InetAddress group, int port) throws IOException {
        if (!group.isMulticastAddress()) { // controllo se è multicast
            throw new IllegalArgumentException();
        }
        this.shutdown = false;
        this.group = group;
        this.port = port;
        this.ms = new MulticastSocket(port);
    }

    /**
     * Questa è l'azione da fare ogni qualvolta arriva un pacchetto
     * da un membro del gruppo multicast
     * @param c client da usare nei metodi specializzati
     */
    public abstract void doSomething(IdClient c);

    /**
     * Questa funzione che dovrebbe essere avviata in un thread separato
     * si collega al gruppo multicast e su mette in attesa di pacchetti.
     * All'arrivo di ogni pacchetto invoca la funzione doSomething.
     */
    public void run() {
        IdClient c = null;
        DatagramPacket dp = null;
        ByteArrayInputStream bufin = null;
        DataInputStream in = null;

        try {
            ms.joinGroup(group); // mi iscrivo al gruppo
            ms.setSoTimeout(Configuration.GM_SOCKET_TIMEOUT);
        } catch (Exception e) {
            System.out.println("GESTORE MULTICAST: "
                    + "impossibile collegarsi al gruppo " + group);
            return;
        }
        dp = new DatagramPacket(new byte[256], 256);

        while (!Thread.interrupted() && !shutdown) {
            try {
                ms.receive(dp); // ricevo e stampo
            } catch (SocketTimeoutException s) {
                continue;
            } catch (Exception e) {
                System.out.println("GESTORE MULTICAST: "
                        + "errore nella ricezione, riprovo tra "
                        + Configuration.GM_SOCKET_TIMEOUT + "millisecondi");
                try {
                    Thread.sleep(Configuration.GM_SOCKET_TIMEOUT);
                } catch (InterruptedException ex) {
                    break;
                }
                continue;
            }
            bufin = new ByteArrayInputStream(dp.getData(), 0, dp.getLength());
            in = new DataInputStream(bufin);
            String s = null;
            try {
                s = in.readUTF();
            } catch (IOException ex) {
                System.out.println("GESTORE MULTICAST: "
                        + "è arrivato un pacchetto non valido, lo ignoro");
                continue;
            }
            System.out.println("GESTORE MULTICAST: "
                    + "letto il paccheto " + s);
            try {
                c = new IdClient(s);
            } catch (Exception e) {
                System.out.println("GESTORE MULTICAST: "
                        + s + " non è un client valido.");
                continue;
            }
            if (c != null) {
                doSomething(c);
            }
            c = null;
        }
        System.out.println("GESTORE MULTICAST: "
                + "sono stato interrotto, termino.");
    }
}
