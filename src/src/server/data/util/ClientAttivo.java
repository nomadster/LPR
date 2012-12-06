package server.data.util;

import java.util.Iterator;
import java.util.LinkedList;
import common.identifier.FileDescriptor;

/**
 * La classe ClientAttivo mantiene la lista dei file posseduti e richiesti
 * da un client
 * @author Federico Della Bona - Alessandro Lensi
 */
public class ClientAttivo {

    private LinkedList<FileDescriptor> file_posseduti;
    private LinkedList<FileDescriptor> file_richiesti;


    /**
     * Crea un ClientAttivo senza file posseduti e richiesti
     */
    public ClientAttivo(){
        this.file_posseduti = new LinkedList<FileDescriptor>();
        this.file_richiesti = new LinkedList<FileDescriptor>();
    }

    /**
     * Aggiunge f alla lista dei file posseduti
     * @param f file da aggiungere
     */
    public void addFilePosseduto(FileDescriptor f){
        this.file_posseduti.addFirst(f);
    }

    /**
     * Aggiunge f alla lista dei file richiesti
     * @param f file da aggiungere
     */
    public void addFileRichiesto(FileDescriptor f){
        this.file_richiesti.add(f);
    }

    /**
     * rimuove f dalla lista dei file richiesti
     * @param f file da rimuovere
     * @return true se la lista contiene f, false altrimenti
     */
    public boolean removeFileRichiesto(FileDescriptor f){

        return this.file_richiesti.remove(f);
    }

    /**
     * Controlla se f è contenuto tra i file posseduti
     * @param f file da controllare
     * @return true se il file è contenuto, false altrimenti
     */
    public boolean containsNeiPosseduti(FileDescriptor f){
        return this.file_posseduti.contains(f);
    }

    /**
     * Controlla se f è contenuto tra i file richiesti
     * @param f file da controllare
     * @return true se il file è contenuto, false altrimenti
     */
    public boolean containsNeiRichiesti(FileDescriptor f){
        return this.file_richiesti.contains(f);
    }

    /**
     * Restituisce un iteratore per i file posseduti
     * @return l'iteratore
     */
    public Iterator scanPosseduti(){
        return this.file_posseduti.iterator();
    }

    /**
     * Restituisce un iteratore per i file richiesti
     * @return l'iteratore
     */
    public Iterator scanRichiesti(){
        return this.file_richiesti.iterator();
    }

    /**
     * Stampa a video le liste di questo oggetto
     */
    public void stampa(){
        Iterator gen_posseduti = scanPosseduti();
        Iterator gen_richiesti = scanRichiesti();
        System.out.println("FileDescriptor posseduti: ");
        while(gen_posseduti.hasNext()){
            System.out.println(gen_posseduti.next());
        }
        System.out.println("FileDescriptor richiesti: ");
        while(gen_richiesti.hasNext()){
            System.out.println(gen_richiesti.next());
        }
    }

}


