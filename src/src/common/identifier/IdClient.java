package common.identifier;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * La classe IdClient realizza un identificatore di client, e racchiude
 * InetAddress e porta del client
 * @author Federico Della Bona - Alessandro Lensi
 */
public class IdClient implements Serializable{


    private InetAddress ip;
    private int porta;

    /**
     * Crea un nuovo identificatore di client a partire da un
     * InetAddress e una porta
     * @param ip InetAddress del client
     * @param porta porta del client
     */
    public IdClient(InetAddress ip, int porta){
        this.ip = ip;
        this.porta = porta;
    }

    /**
     * Crea un nuovo IdClient a partire da una stringa che deve essere
     * nel formato ip:port (esempio "192.168.0.1:4662")
     * @param id stringa che rappresenta il client
     * @throws UnknownHostException se non può essere trovato un host per
     * l'indirizzo ip passato nell'argomento
     */
    public IdClient(String id) throws UnknownHostException{
        String[] array = id.split(":", 2);
        this.ip = InetAddress.getByName(array[0]);
        this.porta = Integer.parseInt(array[1]);
    }
    
    /**
     * Restituisce l'InetAddress
     * @return l'InetAddress
     */
    public InetAddress getAddress(){
        return this.ip;
    }

    /**
     * Restituisce la porta
     * @return la porta
     */
    public int getPort() {
        return this.porta;
    }

    /**
     * Restituisce una stringa che rappresenta l'IdClient
     * @return una stringa che rappresenta l'IdClient
     */
    @Override
    public String toString(){
        return this.ip.getHostAddress().toString()+":"+this.porta;
    }


    /**
     * Controlla se i due oggetti sono equivalenti.
     * L'equivalenza deve essere riflessiva, simmetrica, transitiva,
     * consistente.
     * @param o l'oggetto da confrontare con this
     * @return true se gli oggetti si equivalgono, false altrimenti
     */
    @Override
    public boolean equals(Object o){
        if (o.getClass() == IdClient.class)
            return this.toString().equals(((IdClient)o).toString());
        return false;
    }

    /**
     * Restituisce un valore hashcode per l'oggetto
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.ip != null ? this.ip.hashCode() : 0);
        hash = 71 * hash + this.porta;
        return hash;
    }

    
}
