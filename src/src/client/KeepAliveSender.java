package client;

import common.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import common.identifier.IdClient;

/**
 * La classe KeepAliveSender realizza un modulo del client che
 * invia periodicamente delle notifiche al server
 * per dimostrare che il client che la usa è attivo.
 * A ricevere queste notifiche c'è la classe GestoreKeepAlive che complementa 
 * la classe KeepAliveSender sul lato server.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class KeepAliveSender implements Runnable {

    private InetAddress host;
    private int port;
    private IdClient self;

    /**
     * Inizializza un KeepAliveSender
     * @param host indirizzo del server
     * @param port porta del server
     * @param self client che usa il KeepAliveSender
     */
    public KeepAliveSender(InetAddress host, int port, IdClient self) {
        this.host = host;
        this.port = port;
        this.self = self;
    }

    /**
     * Questa funzione che dovrebbe essere lanciata su un thread
     * a parte, invia periodicamente delle notifiche al server, per
     * dimostrare lo stato di attività del client
     */
    public void run() {
        DatagramSocket sender = null;
        try {
            sender = new DatagramSocket();
            sender.connect(host, port);
        } catch (Exception e) {
            System.out.println("KEEP ALIVE SENDER: " + this.self
                    + " socket non trovata");
        }

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buf);
        try {
            out.writeUTF(this.self.toString());
        } catch (IOException ex) {
            System.out.println("KEEP ALIVE SENDER: " + this.self
                    + " errore di codificazione del client in UTF");
        }

        System.out.println("KEEP ALIVE SENDER: " + this.self
                + " inserisco " + buf.toString() + "in UTF nel pacchetto "
                + "e inizio ad inviarlo");

        DatagramPacket packet = new DatagramPacket(
                buf.toByteArray(), buf.size());



        while (!Thread.interrupted()) {
            try {
                System.out.println("KEEP ALIVE SENDER: " + this.self
                        + " ho mandato un pacchetto...");
                sender.send(packet);
                Thread.sleep(Configuration.GKA_KEEPALIVE_FREQUENCY);
            } catch (InterruptedException i) {
                break;
            } catch (Exception e) {
                System.out.println("KEEP ALIVE SENDER: " + this.self
                        + " errore nell'invio del pacchetto keep-alive");
                try {
                    Thread.sleep(Configuration.GKA_KEEPALIVE_FREQUENCY);
                } catch (InterruptedException ex) {
                    break;
                }
                continue;
            }
        }
        System.out.println("KEEP ALIVE SENDER: " + this.self
                + "termino");
    }
}
