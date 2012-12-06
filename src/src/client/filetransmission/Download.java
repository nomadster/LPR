package client.filetransmission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import common.identifier.FileDescriptor;
import common.identifier.IdClient;
import common.remote.ServerRemote;
import common.remote.SearchResult;
import common.Configuration;

/**
 * Realizza il Task incaricato di scaricare un file da un altro client.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class Download implements Runnable {

    String filename;
    String workingDir;
    InetAddress ip;
    int port;
    ServerRemote stub;
    IdClient myself;
    FileDescriptor toDownload;

    /**
     * Crea un nuovo Thread di download. 
     * Assumiamo che i parametri siano sempre diversi da null, quindi
     * chi chiama il costruttore deve esserne sicuro!
     * @param sr Struttura contenente il FileDescriptor (del package oggetti)
     * che rappresenta il file da reperire e l'IdClient di chi lo possiede.
     * @param workingDir La directory dove salvare il file
     * una volta terminato il download
     * @param stub Lo stub remoto del server, per 
     * notificare il completamento (o eventuali errori) di un download
     * @param self L'IdClient che rappresenta il client che richiede il file.
     * In caso di fallimenti durante il download, dobbiamo notificarlo
     * al Server
     */
    public Download(SearchResult sr, String workingDir,
            ServerRemote stub, IdClient self) {

        this.filename = sr.getFileDescriptor().getName();
        this.ip = sr.getSeeder().getAddress();
        this.port = sr.getSeeder().getPort();
        this.workingDir = workingDir;
        this.stub = stub;
        this.myself = self;
        this.toDownload = sr.getFileDescriptor();
    }

    /**
     * Funzione che viene eseguita quando si chiama la start() di Thread.
     * Si occupa di scaricare il file dal client indicato.
     */
    @Override
    public void run() {

        Socket transferSocket = null;

        File fileToDownload =
                new File(this.workingDir + Configuration.GTF_TMP_DIR + filename);

        InputStream is = null;
        ObjectInputStream in = null;
        OutputStream os = null;
        ObjectOutputStream out = null;
        String response = null;


        /* Controlliamo che non ci sia un file con lo stesso nome rimasto nella
         * cartella temporanea in seguito ad un tentativo di download
         * precedentemente fallito. Se è questo il caso, lo cancelliamo.
         */
        if (fileToDownload.exists()) {
            if (fileToDownload.canRead() && fileToDownload.canWrite()) {
                fileToDownload.delete();
            }

            /* Questo codice serve solo se si vuole prevenire il caso
             * in cui qualcuno può creare una cartella temporanea
             * con lo stesso nome che abbiamo scelto noi,
             * dentro la cartella del nostro client
             * e salvarci dentro un file che non possiamo cancellare
             * else {
             * System.out.println("Download "
             *      + filename
             *      + ": Esiste già un file che si chiama cosi");
             * try{
             *  stub.transferFailed(filename, myself);
             * } catch(RemoteException e){
             * System.out.println("DOWNLOAD"
             *      + Thread.currentThread().getName()
             *      + "Eccezione remota:\n"
             *      + e);
             * } finally{
             *  return;
             * }
             * }
             */
        }

        try {
            transferSocket = new Socket(ip, port);
            is = transferSocket.getInputStream();
            in = new ObjectInputStream(is);
            os = transferSocket.getOutputStream();
            out = new ObjectOutputStream(os);

            /* Richiedo filename */
            out.writeObject(filename);

            response = (String) in.readObject();

            if (!response.equalsIgnoreCase("ok")) {
                System.out.println("GESTORE TRASMISSIONE FILE: Download("
                        + Thread.currentThread().getName()
                        + "), il client "
                        + ip.toString() + ":" + port
                        + " non ha il file" + filename
                        + "\nTERMINO");

                transferSocket.close();
                fileToDownload.delete();
                /* Notifico al server che ci sono stati problemi
                 * nel trasferire il file
                 */
                stub.transferFailed(filename, myself);
                return;
            }

            System.out.println("GESTORE TRASMISSIONE FILE: Download("
                    + Thread.currentThread().getName()
                    + "), Scaricamento di " + filename
                    + " iniziato.");

            byte[] buf = new byte[1024];
            int read = 0;
            FileOutputStream fos = new FileOutputStream(fileToDownload);


            while ((read = in.read(buf)) != -1) {
                fos.write(buf, 0, read);
            }

            fos.flush();
            fos.close();
            in.close();
            out.close();
            transferSocket.close();

        } catch (Exception e) {
            /* Qualcosa è andato storto durante il trasferimento del file */
            System.out.println("GESTORE TRASMISSIONE FILE: Download("
                    + Thread.currentThread().getName()
                    + "), errore durante il trasferimento di " + filename
                    + "\n Eccezione: " + e
                    + "\nTERMINO");
            try {
                transferSocket.close();
            } catch (IOException ex) {
            }

            /* elimino il file temporaneo */
            fileToDownload.delete();

            try {
                /* notifico al server il problema di trasferimento */
                stub.transferFailed(filename, myself);
            } catch (RemoteException ex) {
            }
            return;
        }

        /* controllo che il file sia stato effettivamente ricevuto per intero*/
        if (fileToDownload.length() == toDownload.getSize()) {
            System.out.println("GESTORE TRASMISSIONE FILE: Download("
                    + Thread.currentThread().getName()
                    + "), " + filename
                    + "Ricevuto correttamente. Lo sposto in " + workingDir);

            fileToDownload.renameTo(new File(workingDir + "/"+ filename));

            try {
                stub.completed(myself, toDownload);
            } catch (RemoteException e) {
                System.out.println("GESTORE TRASMISSIONE FILE: Download("
                        + Thread.currentThread().getName()
                        + "), COMPLETED FALLITA.");
                return;
            }
        } else {
            /* il file NON è stato ricevuto per intero */
            System.out.println("GESTORE TRASMISSIONE FILE: Download("
                    + Thread.currentThread().getName()
                    + "), errore durante il trasferimento di " + filename
                    + " il file non è stato ricevuto completamente"
                    + "\nTERMINO");
            try {
                /* notifico al server il problema di trasferimento */
                stub.transferFailed(filename, myself);
            } catch (RemoteException ex) {
            }

            fileToDownload.delete();
        }

        /* stampiamo che la completed è andata OK */
        System.out.println("GESTORE TRASMISSIONE FILE: Download("
                + Thread.currentThread().getName()
                + "), COMPLETED SUCCESSO. Da ora in poi "
                + myself.toString() + " è Seeder di " + filename);




        return;

    }
}
