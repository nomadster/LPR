package server.keepalive;

import common.identifier.IdClient;
import common.Configuration;
import server.data.DataServerInterface;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Timer;

/**
 * La classe GestoreKeepAlive realizza un modulo del server per
 * controllare periodicamente che i client registrati siano attivi.
 * Questa classe è complementata sul lato client dalla classe
 * KeepAliveSender, che deve notificare periodicamente a questo gestore
 * lo stato di attività del client
 * @author Federico Della Bona - Alessandro Lensi
 */
public class GestoreKeepAlive extends Thread {

    private int porta;
    private final HashMap hashtimer;
    private DataServerInterface data;

    /**
     * Inizializza un GestoreKeepAlive pronto ad essere avviato
     * @param data Il DataServer dove rimuovere un client inattivo
     * @param porta porta dove mettersi in ascolto del KeepAliveSender
     */
    public GestoreKeepAlive(DataServerInterface data, int porta) {
        this.porta = porta;
        this.data = data;
        hashtimer = new HashMap();
    }

    private DatagramSocket creaSocket() {
        DatagramSocket ds;
        try {
            ds = new DatagramSocket(this.porta);
        } catch (SocketException e) {
            System.out.println("GESTORE DEI KEEP-ALIVE: "
                    + "impossibile creare il socket.");
            return null;
        }
        return ds;
    }

    /**
     * Aggiunge il client id all'insieme dei client controllati
     * dal gestore.
     * @param id nuovo client da controllare
     * @return true se id è attivo nel dataserver, false altrimenti
     */
    public boolean startTimer(IdClient id) {
        if (!this.data.isActive(id)) {
            return false;
        }
        boolean isDaemon = true;
        KillClient killer = new KillClient(data, this, id);
        Timer t = new Timer(isDaemon);
        TimerKiller tk = new TimerKiller(killer, t);
        synchronized (hashtimer) {
            this.hashtimer.put(id, tk);
        }
        t.schedule(killer, 3000); //mettere come costante
        return true;
    }

    /**
     * Elimina il client id dall'insieme dei client controllati
     * dal gestore
     * @param id client da rimuovere
     */
    protected void deleteTimer(IdClient id) {
        synchronized (hashtimer) {
            hashtimer.remove(id);
        }
    }

    private boolean resetTimer(IdClient id) {
        if (!this.data.isActive(id)) {
            return false;
        }
        TimerKiller tk;
        synchronized (hashtimer) {
            tk = (TimerKiller) hashtimer.get(id);
        }
        System.out.println("GESTORE DEI KEEP-ALIVE: resetto il timer "
                + "di [" + id + "]");
        if (tk != null) {
            tk.reset();
        }
        return true;
    }

    /**
     * Funzione che viene eseguita quando si chiama la start() di Thread.
     * Si mette ciclicamente in attesa di notifiche da parte dei
     * KeepAliveSender dei vari client registrati. I client che non
     * notificano in tempo la loro attività vengono eliminati dal DataServer
     */
    @Override
    public void run() {
        DatagramSocket receiver = creaSocket();
        if (receiver == null) {
            System.out.println("GESTORE DEI KEEP-ALIVE: Esco");
            return;
        }
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, 256);
        ByteArrayInputStream bufin = null;
        DataInputStream in = null;

        IdClient idclient;

        try {
            receiver.setSoTimeout(Configuration.GKA_SOCKET_TIMEOUT);
        } catch (SocketException ex) {
            System.out.println(ex.toString());
        }

        System.out.println(
                "GESTORE DEI KEEP-ALIVE: "
                + "In attesa di pacchetti...");
        while (!Thread.interrupted()) {
            try {
                receiver.receive(packet);
            } catch (SocketTimeoutException s) {
                continue;
            } catch (Exception e) {
                System.out.println("GESTORE DEI KEEP-ALIVE: "
                        + "errore nella ricezione dei pachetti keep-alive");
                try {
                    Thread.sleep(Configuration.GKA_SOCKET_TIMEOUT);
                } catch (InterruptedException ex) {
                    break;
                }
                continue;
            }
            bufin = new ByteArrayInputStream(
                    packet.getData(), 0, packet.getLength());

            in = new DataInputStream(bufin);
            String s = null;
            try {
                s = in.readUTF();
            } catch (IOException ex) {
                System.out.println("GESTORE DEI KEEP-ALIVE: "
                        + "pacchetto non valido, lo ignoro.");
                continue;
            }
            try {
                idclient = new IdClient(s);
            } catch (Exception e) {
                System.out.println("GESTORE DEI KEEP-ALIVE: "
                        + s + " non è un client valido.");
                continue;
            }

            System.out.println("GESTORE DEI KEEP-ALIVE: "
                    + "resetto il timer di " + idclient.toString());
            resetTimer(idclient);
        }
        System.out.println("GESTORE DEI KEEP-ALIVE: "
                + "sono stato interrotto, termino");
    }
}
