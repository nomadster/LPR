package client.filetransmission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Realizza il Task incaricato di inviare un file ad un altro client.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class Upload implements Runnable {

    Socket transferSocket;
    String workingDir;

    /**
     * Crea un nuovo Thread di upload.
     * Assumiamo che i parametri siano sempre diversi da null, quindi
     * chi chiama il costruttore deve esserne sicuro!
     * @param workingDir La directory che contiene il file da inviare
     * @param transferSocket Il socket dove inviare il file
     */
    public Upload(String workingDir, Socket transferSocket) {
        this.workingDir = workingDir;
        this.transferSocket = transferSocket;
    }

    /**
     * Funzione che viene eseguita quando si chiama la start() di Thread.
     * Si occupa di gestire la richiesta di upload da parte di un client.
     */
    @Override
    public void run() {
        String filename = null;
        OutputStream os = null;
        ObjectOutputStream out = null;
        InputStream is = null;
        ObjectInputStream in = null;

        try {
            os = transferSocket.getOutputStream();
            is = transferSocket.getInputStream();
            out = new ObjectOutputStream(os);
            in = new ObjectInputStream(is);

            /* Il client che richiede il file invia il filename, lo leggo */
            filename = (String) in.readObject();

            File uploaded = new File(workingDir + "/" + filename);
            /* Controllo di possedere effettivamente il file richiesto */
            if (uploaded.exists() && uploaded.isFile()) {
                /* Indico di possedere il file richiesto */
                out.writeObject("ok");

                byte[] buf = new byte[1024];
                int read = 0;
                FileInputStream fis = new FileInputStream(uploaded);

                System.out.println(
                        "GESTORE TRASMISSIONE FILE: Upload("
                        + Thread.currentThread().getName()
                        + "), Possiedo il file " + filename + ". Lo invio");

                while ((read = fis.read(buf)) != -1) {
                    out.write(buf, 0, read);
                }

                fis.close();
                out.flush();
                out.close();
                in.close();
                transferSocket.close();

                System.out.println(
                        "GESTORE TRASMISSIONE FILE: Upload("
                        + Thread.currentThread().getName()
                        + "), Trasferimento di " + filename + " COMPLETATO");

            } else {
                System.out.println(
                        "GESTORE TRASMISSIONE FILE: Upload("
                        + Thread.currentThread().getName()
                        + "), Non possiedo il file " + filename + "!");
                out.writeObject("notfound");
            }
            transferSocket.close();
        } catch (Exception e) {

            System.out.println(
                    "GESTORE TRASMISSIONE FILE: Upload("
                    + Thread.currentThread().getName()
                    + "), Problemi durante il trasferimento di " + filename
                    + "\nEccezione:" + e
                    + "\nTERMINO");

            try {
                transferSocket.close();
            } catch (IOException ex) {
            }
        }
    }
}
