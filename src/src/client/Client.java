package client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
/*----------------*/
import common.identifier.FileDescriptor;
import common.identifier.IdClient;
import common.remote.exception.ClientAlreadyRegisteredException;
import common.remote.exception.ClientNotActiveException;
import common.remote.ClientRemote;
import common.remote.ServerRemote;
import common.remote.SearchResult;
import client.filetransmission.GestoreTrasmissioneFile;
import common.Configuration;
import common.multicast.GestoreMulticastClient;

/**
 * La classe Client è il cuore dell'applicazione client. Offre metodi
 * per avviare e terminare l'esecuzione e metodi remoti per mezzo dei quali
 * il server interagisce con essa.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class Client implements ClientRemote {

    private String workingDir;
    private ServerRemote stub;
    private String serverHost;
    private int udpPort;
    private InetAddress multicastGroup;
    private IdClient myself;
    private ClientRemote callback;
    private listaClient listaClients;
    private KeepAliveSender gestoreKeepAlive;
    private Thread gestoreKeepAliveThread;
    private GestoreMulticastClient gestoreMulticast;
    private Thread gestoreMulticastThread;
    private GestoreTrasmissioneFile gestoreTCP;
    private Thread gestoreTCPThread;

    /**
     * Costruisce un nuovo oggetto Client. Assume che i parametri siano 
     * sensati quindi chi lo usa deve assicurarsene.
     * @param stub Lo stub remoto del server
     * @param workingDir La directory che contiene i files
     * @param serverHost L'host del server
     * @param udpPort La porta UDP sulla quale inviare i pacchetti
     *                keepalive
     * @param multicastGroup Il gruppo multicast da joinare
     */
    public Client(ServerRemote stub, String workingDir, String serverHost,
            int udpPort, InetAddress multicastGroup) {

        this.workingDir = workingDir;
        this.stub = stub;
        this.serverHost = serverHost;
        this.udpPort = udpPort;
        this.multicastGroup = multicastGroup;


        this.myself = null;
        this.callback = null;
        this.listaClients = new listaClient();//almeno non è mai NULL
        this.gestoreKeepAliveThread = null;
        this.gestoreMulticast = null;
        this.gestoreMulticastThread = null;
        this.gestoreTCP = null;
        this.gestoreTCPThread = null;


    }

    /**
     * Metodo che si occupa di avviare l'esecuzione di tutti i moduli del 
     * client.
     * @throws Exception quando ci sono problemi ad avviare qualcosa
     */
    public void startClient() throws Exception {

        try {
            callback = (ClientRemote) UnicastRemoteObject.exportObject(
                    (ClientRemote) this, 0);
        } catch (RemoteException e) {
            System.out.println("CLIENT, problemi nell'esportare "
                    + "l'oggetto remoto.");
            throw new Exception(e);
        }

        /* Avvio il gestore TCP */
        try {
            gestoreTCP = new GestoreTrasmissioneFile(workingDir, stub);
        } catch (Exception e) {
            System.out.println("CLIENT, problemi nell'avviare "
                    + "il gestore TCP");
            throw new Exception(e);
        }

        gestoreTCPThread = new Thread(gestoreTCP, "GestoreTCP");
        gestoreTCPThread.start();
        System.out.println(
                "CLIENT,"
                + " Gestore TCP avviato con successo."
                + "\n TID->"
                + gestoreTCPThread.getName());

        /* Costruisco il mio IdClient (this.myself)
         * L'ip e la porta me li da il gestoreTCP
         */
        myself = new IdClient(gestoreTCP.getAddress(), gestoreTCP.getPort());


        /* Avvio del gestoreKeepAlive */
        try {
            gestoreKeepAlive = new KeepAliveSender(
                    InetAddress.getByName(serverHost), udpPort, this.myself);
        } catch (UnknownHostException e) {
            System.out.println("CLIENT, problemi nell'avviare "
                    + "il gestore Keepalive");
            throw new Exception(e);
        }

        gestoreKeepAliveThread =
                new Thread(gestoreKeepAlive, "Gestore KeepAliveClient");
        gestoreKeepAliveThread.start();
        System.out.println(
                "CLIENT,"
                + " Gestore Keepalive avviato con successo."
                + "\n TID->"
                + gestoreKeepAliveThread.getName());


        try {
            this.listaClients =
                    new listaClient(stub.register(myself, callback));
        } catch (ClientAlreadyRegisteredException e) {
            /* Sono già registrato, non dico niente all'utente
             * quindi non faccio niente quando ricevo
             * questa eccezione
             */
        } catch (RemoteException ee) {
            System.out.println(
                    "CLIENT,"
                    + " Problemi a comunicare con il server:\n"
                    + serverHost);
            throw new Exception(ee);
        }

        System.out.println("CLIENT, REGISTER avvenuta con successo");



        /* avvio il gestore Multicast */
        List multilist = listaClients.returnList();
        if (multilist == null) {
            throw new Exception("MULTILIST E' NULL");
        }
        try {
            gestoreMulticast =
                    new GestoreMulticastClient(
                    multicastGroup, Configuration.GM_DEFAULT_MULTICAST_PORT,
                    multilist);
        } catch (Exception e) {
            throw new Exception(e);
        }

        gestoreMulticastThread =
                new Thread(gestoreMulticast, "GestoreMulticast");
        gestoreMulticastThread.start();
        System.out.println(
                "CLIENT,"
                + " Gestore Multicast avviato con successo."
                + "\n TID->"
                + gestoreKeepAliveThread.getName());
    }

    /**
     * Termina il client e tutti i suoi moduli.
     * @throws InterruptedException se una join() viene interrotta
     */
    public void stopClient() throws InterruptedException {

        System.out.println("Invio pacchetto multicast!");
        try {
            this.gestoreMulticast.close(this.myself);
        } catch (IOException e) {
            System.out.println(
                    "Errore di I/O nell'inviare il pacchetto Multicast.");
        }

        System.out.println("Termino Gestore KeepAlive ->");
        this.gestoreKeepAliveThread.interrupt();
        this.gestoreKeepAliveThread.join();
        System.out.println("Gestore Keep Alive Terminato\n");

        System.out.println("Termino Gestore Multicast ->");
        this.gestoreMulticastThread.interrupt();
        this.gestoreMulticastThread.join();
        System.out.println("Gestore Multicast Terminato\n");

        System.out.println("Termino Gestore TCP ->");
        this.gestoreTCPThread.interrupt();
        this.gestoreTCPThread.join();
        System.out.println("Gestore TCP Terminato\n");

        System.err.println("Client Terminato.");
        System.exit(0);
    }

    /**
     * Metodo remoto con il quale il server notifica al client
     * la propria imminente terminazione
     * @throws RemoteException Non la lancia
     */
    @Override
    public void serverShuttingDown() throws RemoteException {
        try {
            System.out.println("CLIENT, il server sta terminando. Esco.");
            this.stopClient();
        } catch (InterruptedException ex) {
            System.out.println("CLIENT, problemi con la serverShuttingDown()");
        }
    }

    /**
     * Aggiunge l’identificatore del client passato per
     * argomento alla lista di client attivi.
     * @param c Il client da aggiungere
     * @throws RemoteException Non la lancia
     */
    @Override
    public synchronized void addClient(IdClient c) throws RemoteException {

        /* devo ignorare callback addClient su me stesso */
        if (c.equals(this.myself)) {
            return;
        }

        try {
            this.listaClients.addClient(c);
        } catch (Exception e) {
            return;
        }
        System.out.println("CLIENT, ADDCLIENT:\n"
                + "Lista client aggiornata -> " + listaClients);
        return;
    }

    /**
     * Rimuove l’identificatore del client passato per
     * argomento dalla lista di client attivi.
     * @param c Il client da rimuovere
     * @throws RemoteException Non la lancia
     */
    @Override
    public synchronized void removeClient(IdClient c) throws RemoteException {
        boolean isIn = false;
        try {
            isIn = this.listaClients.removeClient(c);
        } catch (Exception e) {
            return;
        }
        if (isIn) {
            System.out.println("CLIENT, REMOVECLIENT:\n"
                    + "Lista client aggiornata -> " + listaClients);

        }
    }

    /**
     * Metodo per invocare la publish sul server remoto.
     * Assume che i parametri siano sensati!
     * @param fileName Il file da pubblicare
     * @param byteSize La sua dimensione
     * @throws RemoteException Se non siamo registrati
     * e non riusciamo a registrarci
     */
    public void publishFile(String fileName, int byteSize)
            throws RemoteException {
        FileDescriptor fd = new FileDescriptor(fileName, byteSize);
        boolean ret = false;

        try {
            ret = this.stub.publish(myself, fd);
        } catch (ClientNotActiveException e) {
            /* non ero registrato provo a registrarmi di nuovo
             * Se la register fallisce, termino il client.
             */
            this.listaClients =
                    new listaClient(this.stub.register(myself, callback));
            ret = this.stub.publish(myself, fd);
        } catch (RemoteException ex) {
            throw ex;
        }

        if (ret == true) {
            System.out.println("CLIENT, file " + fileName
                    + "già pubblicato da un altro client");
        } else {
            System.out.println("CLIENT, sei seeder per " + fileName);
        }

        return;

    }

    /**
     * Metodo per invocare la search sul server remoto.
     * @param fileName il file da cercare
     * @throws RemoteException Se non siamo registrati
     * e non riusciamo a registrarci
     */
    public void searchFile(String fileName) throws RemoteException {
        SearchResult sr = null;
        try {
            sr = this.stub.search(myself, fileName);
        } catch (ClientNotActiveException e) {
            /* non ero registrato provo a registrarmi di nuovo
             * Se la register fallisce, termino il client.
             */
            this.stub.register(myself, callback);
            sr = this.stub.search(myself, fileName);
        } catch (RemoteException ex) {
            throw ex;
        }

        if (sr == null) {
            System.out.println("CLIENT, file " + fileName
                    + "non disponibile per il download");

            return;
        } else {

            System.out.println("CLIENT, file " + fileName
                    + "trovato. Lo scarico.");
            this.gestoreTCP.download(sr);

            return;
        }

    }

    private static void printUsage() {
        System.out.println("\nProgetto LPR - Della Bona, Lensi: CLIENT\n"
                + "\n"
                + "Utilizzo:\n"
                + "java -jar Client.jar workingDir\n"
                + "oppure\n"
                + "java -jar Client.jar workingDir [opzioni]\n"
                + "\n"
                + "Opzioni:\n"
                + "- serverHost, l'host su cui viene eseguito il server\n"
                + "- rmiPort, la porta su cui è in ascolto il server\n"
                + "- UDPPort, la porta dove inviare i pacchetti keepalive\n"
                + "- MulticastGroup, gruppo multicast su quale inviare i pacchetti\n"
                + "\n"
                + "Ogni opzione rende obbligatorio l'inserimento delle precedenti\n"
                + "\n");

    }

    public static void main(String[] args) throws InterruptedException {

        String workingDirName;
        String serverHost;
        int rmiPort;
        int udpPort;
        String multicastString;
        File workingDir;
        Registry registry;
        ServerRemote stub;
        InetAddress multicastGroup;
        Client client;

        /***Parsing degli argomenti***/
        if (args.length > 5) {
            Client.printUsage();
            return;
        }
        /*Parso la workingDir [argomento obbligatorio]*/
        workingDirName = (args.length < 1) ? null : args[0];

        /*Parso il serverhost */
        serverHost = (args.length < 2)
                ? Configuration.CLT_DEFAULT_SERVER_HOST : args[1];

        /* Parso la porta RMI */
        try {
            rmiPort = (args.length < 3)
                    ? Configuration.SRV_DEFAULT_RMI_PORT
                    : Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("MAIN, portaRMI deve essere un intero.");
            return;
        }

        /* Parso la porta UDP */
        try {
            udpPort = (args.length < 4)
                    ? Configuration.SRV_DEFAULT_UDP_PORT
                    : Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("MAIN, portaUDP deve essere un intero.");
            return;
        }

        /* Parso il gruppo multicast */
        multicastString = (args.length < 5)
                ? Configuration.GM_DEFAULT_MULTICAST_GROUP : args[4];

        /***fine del parsing degli argomenti***/
        /***controllo dei parametri parsati***/

        /* controllo workingDir */
        if (workingDirName == null) {
            System.out.println("MAIN, workingDir, argomento obbligatorio!");
            Client.printUsage();
            return;
        } else {
            /* devo controllare che sia una directory esistente */
            workingDir = new File(workingDirName);
            if (!workingDir.exists() || !workingDir.isDirectory()) {
                System.out.println("MAIN, la directory specificata non esiste");
                Client.printUsage();
                return;
            }
        }

        /* controllo la porta RMI */
        if (!Configuration.isPort(rmiPort)) {
            System.out.println("MAIN, portaRMI non è una porta valida");
            Client.printUsage();
            return;
        }

        try {
            registry = LocateRegistry.getRegistry(serverHost, rmiPort);

        } catch (RemoteException e) {
            System.out.println("MAIN, non c'è un server in ascolto"
                    + " all'indirizzo specificato");
            return;
        }

        /* controllo porta UDP */
        if (!Configuration.isPort(udpPort)) {
            System.out.println("MAIN, portaUDP non è una porta valida");
            Client.printUsage();
            return;
        }


        /* controllo il gruppo multicast */
        try {
            multicastGroup = InetAddress.getByName(multicastString);
        } catch (UnknownHostException e) {
            System.out.println("MAIN, gruppo multicast non valido");
            Client.printUsage();
            return;
        }

        if (!multicastGroup.isMulticastAddress()) {
            System.out.println("MAIN, gruppo multicast non valido");
            Client.printUsage();
            return;
        }
        /***fine controllo parametri***/
        /* cerco l'interfaccia remota del server */
        try {
            stub = (ServerRemote) registry.lookup(Configuration.SRV_SERVICE_NAME);
        } catch (NotBoundException ex) {
            System.out.println("MAIN, servizio "
                    + Configuration.SRV_SERVICE_NAME
                    + " non disponibile.");
            return;
        } catch (RemoteException ex) {
            System.out.println("MAIN, non c'è un server in ascolto"
                    + " all'indirizzo specificato");
            return;
        }


        /* interfaccia trovata e messa dentro stub
         * creo l'oggetto Client
         */
        client = new Client(stub, workingDirName,
                serverHost, udpPort, multicastGroup);


        System.out.println("MAIN, Avvio il client");
        try {
            client.startClient();
        } catch (Exception e) {
            System.out.println("MAIN, Problemi nell'avviare il client");
            System.exit(0);
        }

        /* Apro il file dei comandi */
        FileReader fr = null;
        File f = new File(workingDir + Configuration.CLT_COMMAND_FILENAME);
        String[] command;

        /* controllo se esiste il file dei comandi */
        try {
            fr = new FileReader(f);
        } catch (FileNotFoundException ex) {
            System.out.println("MAIN, file dei comandi non trovato. Esco");
            client.stopClient();
            return;
        }

        BufferedReader in = new BufferedReader(fr);
        String nextline;

        /* Loop che legge da file */
        try {
            while ((nextline = in.readLine()) != null) {
                command = nextline.split(" ", 2);
                if (command[0].equalsIgnoreCase("exit")
                        && command.length == 1) {
                    //esci
                    System.out.println("MAIN, ricevuto comando EXIT");
                    client.stopClient();
                    return;
                } else if (command.length != 2) {
                    System.out.println("MAIN,"
                            + "'" + nextline + "' comando errato ");
                    continue;
                } else if (command[0].equalsIgnoreCase("publish")) {
                    System.out.println("MAIN, ricevuto comando "
                            + "PUBLISH " + command[1]);
                    File toSend = new File(workingDir + "/" + command[1]);
                    if (toSend.exists() && toSend.isFile() && toSend.canRead()) {
                        try {
                            client.publishFile(toSend.getName(), (int) toSend.length());
                        } catch (RemoteException e) {
                            System.out.println("MAIN, "
                                    + "problemi di comunicazione "
                                    + "con il server:\n"
                                    + e);
                            client.stopClient();
                            return;
                        }
                    } else {
                        System.out.println("MAIN, stai tentando di pubblicare"
                                + " un file che non possiedi!");
                    }
                } else if (command[0].equalsIgnoreCase("search")) {
                    //SEARCH fai qualcosa(command[1])
                    System.out.println("MAIN, ricevuto comando "
                            + "SEARCH " + command[1]);
                    try {
                        client.searchFile(command[1]);
                    } catch (RemoteException e) {
                        System.out.println("MAIN, "
                                + "problemi di comunicazione "
                                + "con il server:\n"
                                + e);
                        client.stopClient();
                        return;
                    }

                } else if (command[0].equalsIgnoreCase("wait")) {
                    System.out.println("MAIN, ricevuto comando "
                            + "WAIT " + command[1]);
                    long mill = 0;
                    try {
                        mill = Long.parseLong(command[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("MAIN, comando "
                                + "WAIT " + command[1]
                                + " errato.");
                    }
                    Thread.sleep(mill);
                } else {
                    System.out.println("MAIN,"
                            + "'" + nextline + "' comando errato ");
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("MAIN, problemi nel while "
                    + "che parsa il file dei comandi -> " + f.getName()
                    + "\nEccezione: " + e);
            e.printStackTrace(System.err);
            client.stopClient();
            return;

        }

    }
}
