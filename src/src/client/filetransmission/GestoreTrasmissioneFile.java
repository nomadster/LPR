package client.filetransmission;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import common.identifier.IdClient;
import common.remote.ServerRemote;
import common.remote.SearchResult;
import common.Configuration;

/**
 * Realizza un task che gestisce la trasmissione di files tra clients.
 * Una volta in esecuzione, risponde automaticamente alle richieste
 * di download da parte di altri client e offre un metodo per richiedere
 * lo scaricamento di un file ad un altro host.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class GestoreTrasmissioneFile implements Runnable {

    ServerSocket ss;
    ExecutorService gestoreDownload;
    ExecutorService gestoreUpload;
    String workingDir;
    ServerRemote stub;
    IdClient myself;

    /**
     * Costruisce un nuovo GestoreTrasmissioneFile.
     * Assumiamo che i suoi parametri siano sempre diversi da null
     * e quindi chi usa il costruttore deve sincerarsene
     * @param workingDir La directory dove sono salvati i files scaricati 
     * e da inviare
     * @param stub Lo stub remoto del server
     * (GestoreTrasmissioneFile deve fare la completed)
     * @throws UnknownHostException
     * Quando InetAddress.getLocalHost() fallisce (?_?)
     * @throws IOException
     * Quando non si riesce ad inizializzare un nuovo ServerSocket
     */
    public GestoreTrasmissioneFile(String workingDir, ServerRemote stub)
            throws UnknownHostException, IOException {

        this.ss = new ServerSocket(0, 50, InetAddress.getLocalHost());
        ss.setSoTimeout(Configuration.GTF_SOCKET_TIMEOUT);
        /* le informazioni per creare l'IdClient le prendo 
         * dal socket, così sono consistenti
         */
        this.myself = new IdClient(ss.getInetAddress(), ss.getLocalPort());
        /* Inizializzo il gestore dei Download */
        this.gestoreDownload = Executors.newCachedThreadPool();
        this.gestoreUpload =
                Executors.newFixedThreadPool(Configuration.GTF_MAX_UPLOAD_THREADS);
        this.workingDir = workingDir;
        this.stub = stub;
    }

    /**
     *
     * @return L'InetAddress su cui è attivo il ServerSocket
     */
    public InetAddress getAddress() {
        return myself.getAddress();
    }

    /**
     *
     * @return La porta su cui è attivo il ServerSocket
     */
    public int getPort() {
        return myself.getPort();
    }

    /**
     * Funzione che inizia a scaricare il file contenuto nel SearchResult
     * dall'host contenuto nel SearchResult
     * @param sr Informazioni per reperire un file da un client.
     */
    public void download(SearchResult sr) {
        if (sr == null) {
            return;
        }

        Download d = new Download(sr, workingDir, stub, myself);
        gestoreDownload.execute(d);
    }

    /**
     * Funzione che viene eseguita quando si chiama la start() di Thread. 
     * Si occupa di avviare il Gestore della Trasmissione dei Files
     */
    @Override
    public void run() {

        /* il socket su cui avverrà il trasferimento (upload o download) */
        Socket transferSocket = null;
        /* Directory dove vengono scaricati i files. 
         * Al termine dei download vengono spostati in workingDir
         */
        File tmpDir = new File(workingDir + Configuration.GTF_TMP_DIR);

        /* se la directory temporanea non esiste, la creo */
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }

        /* entro in questo while fino a che il Thread 
         * non riceve una interruzione
         */
        while (!Thread.interrupted()) {
            try {
                transferSocket = ss.accept();
            } catch (SocketTimeoutException e) {
                /* è scaduto il timeout del socket */
                continue;
            } catch (Exception ex) {
                System.out.println(
                        "GESTIONE TRASMISSIONE FILE,"
                        + " problema con il ServerSocket:\n"
                        + ex + "\nTERMINO IL GESTORE TRASMISSIONE FILES");
                break;
            }
            if (transferSocket != null) {
                System.out.println(
                        "GESTIONE TRASMISSIONE FILE: Aggiungo un upload");
                gestoreUpload.execute(
                        (Runnable) new Upload(workingDir, transferSocket));
            }
        }
        /* quando il Thread riceve l'interruzione, 
         * significa che deve terminare
         */

        gestoreDownload.shutdown();
        try {
            System.out.println(
                    "GESTORE TRASMISSIONE FILE: "
                    + "Aspetto che terminino i download");
            gestoreDownload.awaitTermination(
                    Configuration.GTF_AWAIT_DOWNLOAD_TIMEOUT,
                    Configuration.GTF_AWAIT_DOWNLOAD_TIMEOUT_UNIT);

        } catch (InterruptedException ex) {
            System.out.println("GESTORE TRASMISSIONE FILE:"
                    + " awaitTermination su gestoreDownload"
                    + " ha raggiunto il timeout");
        }

        gestoreUpload.shutdown();
        try {
            System.out.println(
                    "GESTORE TRASMISSIONE FILE: "
                    + "Aspetto che terminino gli upload");
            gestoreUpload.awaitTermination(
                    Configuration.GTF_AWAIT_UPLOAD_TIMEOUT,
                    Configuration.GTF_AWAIT_UPLOAD_TIMEOUT_UNIT);
            
        } catch (InterruptedException ex) {
            System.out.println(
                    "GESTORE TRASMISSIONE FILE: "
                    + "awaitTermination su gestoreUpload"
                    + " ha raggiunto il timeout");
        }

        /* Chiudo il transferSocket */
        try {
            ss.close();
        } catch (IOException ex) {
            System.out.println(
                    "GESTORE TRASMISSIONE FILE:"
                    + " eccezione nella chiusura del serverSocket");
        }
        System.out.println("GESTORE TRASMISSIONE FILE: terminato");
    }
}
