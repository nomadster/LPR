package client;

import java.util.List;
import common.identifier.IdClient;

/**
 * Realizza la lista locale dei client attivi.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class listaClient {

    private List<IdClient> lista;

    /**
     * Crea una lista vuota
     */
    public listaClient() {
        lista = null;
    }

    /**
     * Crea una lista inizializzata con l
     * @param l Una lista di IdClient
     */
    public listaClient(List<IdClient> l) {
        if (l == null) {
            throw new NullPointerException("Costruttore lista client, parametro NULL!");
        }
        lista = l;
    }

    /**
     * Aggiunge c alla lista.
     * @param c Il client da aggiungere
     */
    public synchronized void addClient(IdClient c) {
        if (c != null && lista != null) {
            lista.add(c);
        }
    }

    /**
     * Rimuove c dalla lista.
     * @param c Il client da rimuovere
     * @return true se l'elemento è stato rimosso con successo
     * false altrimenti
     */
    public synchronized boolean removeClient(IdClient c) {
        if (c != null && lista != null) {
            return lista.remove(c);
        }
        return false;
    }

    /**
     * Restitiusce la lista dei client
     * @return la lista dei client.
     */
    public synchronized List<IdClient> returnList() {
        return lista;
    }

    /**
     * Restituisce una stringa che rappresenta la lista
     * @return una stringa che rappresenta la lista, null se la lista è vuota
     */
    @Override
    public synchronized String toString() {
        if (lista != null) {
            return lista.toString();
        }
        return null;
    }
}
