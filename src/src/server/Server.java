package server;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
/*----------------*/
import common.Configuration;
import server.keepalive.GestoreKeepAlive;
import server.data.DataServerInterface;
import server.data.DataServer;
import server.data.exception.ClientNotActiveException;
import common.identifier.FileDescriptor;
import common.identifier.IdClient;
import common.remote.ClientRemote;
import common.remote.ServerRemote;
import common.remote.exception.ClientAlreadyRegisteredException;
import common.remote.SearchResult;
import common.multicast.GestoreMulticastServer;

/**
 * La classe Server è il cuore dell'applicazione server. Offre metodi
 * per avviare e terminare l'esecuzione e metodi remoti per mezzo dei quali
 * i client interagiscono con essa.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class Server implements ServerRemote {

    private final DataServerInterface data;
    private GestoreKeepAlive keepAlive;
    private Thread keepAliveThread;
    private GestoreMulticastServer gestoreMulticast;
    private Thread gestoreMulticastThread;
    int portaUDP;
    InetAddress gruppoMulticast;

    /**
     * Costruisce un nuovo oggetto Server. Assume che i parametri siano sensati
     * quindi chi lo usa deve assicurarsene.
     * @param portaUDP la porta UDP su cui attendere pacchetti keepalive
     * @param gruppoMulticast il gruppo multicast su cui ricevere
     * pacchetti multicast
     */
    public Server(int portaUDP, InetAddress gruppoMulticast) {
        this.data = new DataServer();
        this.portaUDP = portaUDP;
        this.gruppoMulticast = gruppoMulticast;
        this.keepAlive = new GestoreKeepAlive(data, portaUDP);
        this.keepAliveThread = null;
        this.gestoreMulticast = null;
        this.gestoreMulticastThread = null;
    }

    /**
     * Metodo che si occupa di avviare l'esecuzione di tutti i moduli del server
     */
    public void startServer() {
        System.out.println(
                "SERVER,"
                + " avvio Gestore Multicast");
        try {
            gestoreMulticast = new GestoreMulticastServer(
                    gruppoMulticast,
                    Configuration.GM_DEFAULT_MULTICAST_PORT,
                    data);

        } catch (IOException ex) {
            System.out.println(
                    "SERVER,"
                    + " il gruppo multicast "
                    + gruppoMulticast
                    + "non è un gruppo valido."
                    + "\nTERMINO.");
            System.exit(0);
        }
        gestoreMulticastThread =
                new Thread(gestoreMulticast, "GestoreMulticast");
        gestoreMulticastThread.start();
        System.out.println(
                "SERVER,"
                + " Gestore Multicast avviato con successo."
                + "\n TID->"
                + gestoreMulticastThread.getName());

        System.out.println(
                "SERVER,"
                + " avvio Gestore KeepAlive");
        keepAliveThread = new Thread(keepAlive, "Gestore KeepAlive");
        keepAliveThread.start();
        System.out.println(
                "SERVER,"
                + " Gestore Multicast avviato con successo."
                + "\n TID->"
                + keepAliveThread.getName());
    }

    /**
     * Metodo che interrompe l'esecuzione del server e di tutti i suoi moduli.
     */
    public void stopServer() throws InterruptedException {
        /* termino il gestore multicast */
        gestoreMulticastThread.interrupt();
        gestoreMulticastThread.join();
        /* termino il gestore keepalive */
        keepAliveThread.interrupt();
        keepAliveThread.join();

        /* notifico a tutti i client che sto chiudendo */
        synchronized (this.data) {
            Iterator i = this.data.scanCallback();
            while (i.hasNext()) {
                ClientRemote cr = (ClientRemote) i.next();
                try {
                    cr.serverShuttingDown();
                } catch (RemoteException ex) {
                }
            }
        }
        System.err.println("SERVER, Goodbye!");
        System.exit(0);
    }

    /**
     * Metodo remoto tramite il quale i client si registrano nel sistema
     * @param client IdClient del client che vuole registrarsi
     * @param callback Callback remota del client che vuole registrarsi
     * @return la lista di IdClient registrati in quel momento nel server
     * @throws ClientAlreadyRegisteredException Se il client risulta
     * già presente nella lista dei client registrati.
     *
     */
    @Override
    public List<IdClient> register(IdClient client, ClientRemote callback)
            throws ClientAlreadyRegisteredException {

        System.out.println(
                "SERVER, ricevuta REGISTER.\n"
                + "client=" + client
                /* stampo la callback solo se è null */
                + ((callback == null) ? ("\n callback=" + callback) : ""));

        if (data.insertClient(client, callback) == false) {
            System.out.println(
                    "SERVER, REGISTER ignorata, "
                    + "il client " + client
                    + " è già presente.\n");
            throw new ClientAlreadyRegisteredException();
        }

        /* il client è inserito nel sistema
         * lo dico al gestore dei keep alive
         */
        keepAlive.startTimer(client);


        /* notifico a tutti i client attualmente registrati
         * che c'è un nuovo client nel sistema
         */
        synchronized (this.data) {
            Iterator i = this.data.scanCallback();
            ClientRemote cr;
            while (i.hasNext()) {
                cr = (ClientRemote) i.next();
                try {
                    cr.addClient(client);
                } catch (RemoteException e) {
                    continue;
                }
            }
        }

        /* tutto ok, ritorno la lista di tutti i client attivi */
        System.out.println(
                "SERVER, REGISTER completata con successo "
                + client);

        return data.getClientList();
    }

    /**
     * Nel caso in cui il file non esista lo inserisce tra quelli pubblicati
     * con il client che lo ha pubblicato come primo seeder
     * @param client Il client che vuole pubblicare il file
     * @param file Il file da pubblicare
     * @return Restituisce false se è già stato pubblicato un file
     * con lo stesso nome, true altrimenti
     * @throws RemoteException Nel caso in cui il client risulti non registrato
     */
    @Override
    public boolean publish(IdClient client, FileDescriptor file)
            throws RemoteException {

        System.out.println(
                "SERVER, ricevuta PUBLISH.\n"
                + "client=" + client
                + ", file=" + file);

        boolean ret;
        try {
            ret = data.insertFile(client, file);
        } catch (ClientNotActiveException e) {
            System.out.println(
                    "SERVER, PUBLISH. Client=" + client + " non registrato.");
            throw new common.remote.exception.ClientNotActiveException();
        }

        data.printDataServer();
        System.out.println(
                "SERVER, PUBLISH completata con successo "
                + client + ", " + file);
        return ret;

    }

    /**
     * Se esiste un seeder per fileName, il server inserisce il client
     * che ha fatto la richiesta tra i leachers del file.
     * @param client Il client che ha fatto la richiesta
     * @param fileName Il file da cercare
     * @return Restituisce null se il file non * pubblicato altrimenti
     * restituisce una struttura contenente l’identificatore di un seeder
     * del file e il descrittore del file.
     * @throws RemoteException Nel caso in cui il client risulti non registrato
     */
    @Override
    public SearchResult search(IdClient client, String fileName)
            throws RemoteException {

        FileDescriptor f;
        IdClient s;

        try {
            f = data.getFile(fileName, client);
        } catch (ClientNotActiveException e) {
            System.out.println(
                    "SERVER, SEARCH. Client=" + client + " non registrato.");
            throw new common.remote.exception.ClientNotActiveException();
        }

        if (f == null) {
            return null;
        }

        s = data.getSeeder(f);
        if (s == null) {
            return null;
        }

        System.out.println(
                "SERVER, SEARCH completata con successo\n"
                + "client=" + client
                + ", fileName=" + fileName);
        return new SearchResult(f, s);
    }


    /**
     * Segnala al server che il client ha finito di scaricare il file.
     * Il server lo sposta dalla lista dei leachers a quella dei seeders.
     * @param client Il Client che ha fatto la richiesta
     * @param file Il file completato
     * @throws RemoteException Nel caso in cui ci siano problemi con il client
     * e/o con il file
     */
    @Override
    public void completed(IdClient client, FileDescriptor file)
            throws RemoteException {

        System.out.println(
                "SERVER, ricevuta COMPLETED.\n"
                + "client=" + client
                + ", file=" + file);

        try {

            data.leacherToSeeder(client, file);
        } catch (Exception e) {

            throw new RemoteException();
        }
    }

    /**
     * Segnala al server che il client ha avuto problemi nello scaricare 
     * il file così lo può rimuovere dalla lista dei leachers
     * @param filename Il file da rimuovere
     * @param client Il client che ha fatto la richiesta
     * @throws RemoteException Nessuna eccezione viene sollevata
     */
    @Override
    public void transferFailed(String filename, IdClient client)
            throws RemoteException {
        this.data.removeLeacher(filename, client);
    }

    /* stampa il classico usage quando vengono dati i parametri sbagliati */
    private static void printUsage() {
        System.out.println("\nProgetto LPR - Della Bona, Lensi: SERVER\n"
                + "\n"
                + "Utilizzo:\n"
                + "java -jar Server.jar\n"
                + "oppure\n"
                + "java -jar Server.java [opzioni]\n"
                + "\n"
                + "Ogni opzione rende obbligatorio l'inserimento delle precedenti\n"
                + "Opzioni:\n"
                + "rmiPort, la porta su cui creare il registro RMI\n"
                + "UDPPort, la porta su cui ricevere i pacchetti keepalive\n"
                + "MulticastGroup, gruppo multicast su quale ricevere i pacchetti\n"
                + "\n"
                + "Ogni opzione rende obbligatorio l'inserimento delle precedenti\n"
                + "\n");
    }

    public static void main(String args[]) throws InterruptedException {

        int portaRMI;
        int portaUDP;
        String multicastString;
        InetAddress multicastGroup;
        ServerRemote stub;


        /***Parsing dei parametri***/
        if (args.length > 3) {
            Server.printUsage();
            return;
        }

        /*parso portaRMI*/
        try {
            portaRMI = (args.length < 1)
                    ? Configuration.SRV_DEFAULT_RMI_PORT
                    : Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("MAIN, portaRMI deve essere un intero.");
            Server.printUsage();
            return;
        }

        /*parso portaUDP*/
        try {
            portaUDP = (args.length < 2)
                    ? Configuration.SRV_DEFAULT_UDP_PORT
                    : Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("MAIN, portaUDP deve essere un intero");
            Server.printUsage();
            return;
        }

        /*parso gruppo Multicast*/
        multicastString = (args.length < 3)
                ? Configuration.GM_DEFAULT_MULTICAST_GROUP
                : args[2];

        /***Fine parsing argomenti***/
        /***controllo semantico degli argomenti***/

        /*controllo portaRMI*/
        if (!Configuration.isPort(portaRMI)) {
            System.out.println("MAIN, portaRMI non è una porta valida");
            Server.printUsage();
            return;
        }

        /*controllo portaUDP*/
        if (!Configuration.isPort(portaUDP)) {
            System.out.println("MAIN, portaUDP non è una porta valida");
            Server.printUsage();
            return;
        }

        /*controllo gruppo Multicast*/
        try {
            multicastGroup = InetAddress.getByName(multicastString);
        } catch (UnknownHostException e) {
            System.out.println("MAIN, gruppo multicast non valido.");
            Server.printUsage();
            return;
        }

        if (!multicastGroup.isMulticastAddress()) {
            System.out.println("MAIN, gruppo multicast non valido.");
            Server.printUsage();
            return;
        }

        /* fine controllo semantico degli argomenti */


        /***Creo l'oggetto server***/
        Server server = null;
        server = new Server(portaUDP, multicastGroup);
        /*** Oggetto Server creato ***/
        /*** Esporto l'interfaccia remota ***/
        try {
            stub = (ServerRemote) UnicastRemoteObject.exportObject((ServerRemote) server, 0);
        } catch (RemoteException e) {
            System.out.println("MAIN, problemi nell'esportare "
                    + "l'oggetto remota.");
            return;
        }
        /***Interfaccia remota esportata***/
        /*** Creo il registro RMI e bindo l'interfaccia remota ***/
        try {
            // "Si suppone che rmiregistry giri sempre su localhost del server"
            Registry registry = LocateRegistry.createRegistry(portaRMI);
            registry.rebind(Configuration.SRV_SERVICE_NAME, stub);
        } catch (Exception e) {
            System.out.println("MAIN, problemi nell'esportare "
                    + "l'interfaccia remota.");
            return;
        }



        System.out.println("MAIN, Avvio il server");
        try {
            server.startServer();
        } catch (IllegalThreadStateException e) {
            System.out.println("MAIN, problemi nell'avviare "
                    + "il server");
            return;
        }


        System.err.println(
                "MAIN, Server attivo.\nPremere un tasto per terminare");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {

            br.readLine();


        } catch (Exception e) {
        }

        server.stopServer();




    }
}
