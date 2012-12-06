package server.data.util;

import java.util.Iterator;
import java.util.LinkedList;
import common.identifier.FileDescriptor;
import common.identifier.IdClient;

/**
 * La classe FileCondiviso mantiene la lista dei seeders e dei leachers
 * per un file descriptor
 * @author Federico Della Bona - Alessandro Lensi
 */
public class FileCondiviso {
    private FileDescriptor fd;
    private LinkedList<IdClient> seeders;
    private LinkedList<IdClient> leachers;

    /**
     * Crea un FileCondiviso senza seeders e senza leachers
     * @param fd FileDescriptor a cui associare le liste
     */
    public FileCondiviso(FileDescriptor fd){
        this.fd = fd;
        this.seeders = new LinkedList<IdClient>();
        this.leachers = new LinkedList<IdClient>();
    }


    /**
     * Aggiunge id come seeder
     * @param id client da aggiungere
     */
    public void addSeeder(IdClient id){
        this.seeders.addFirst(id);
    }

    /**
     * Aggiunge id come leacher
     * @param id client da aggiungere
     */
    public void addLeacher(IdClient id){
        this.leachers.add(id);
    }

    /**
     * Rimuove id come leacher
     * @param id da rimuovere
     * @return true se la lista contiene l'elemento, false altrimenti
     */
    public boolean removeLeacher(IdClient id){
        return this.leachers.remove(id);
    }

    /**
     * Rimuove id come Seeder
     * @param id da rimuovere
     * @return true se la lista contiene l'elemento, false altrimenti
     */
    public boolean removeSeeder(IdClient id){
        return this.seeders.remove(id);
    }

    /**
     * Restituisce il file descriptor di questo file condiviso
     * @return Il FileDescriptor
     */
    public FileDescriptor getFileDescriptor(){
        return this.fd;
    }

    /**
     * Restituisce un seeder per questo FileCondiviso
     * @return un IdClient seeder del FileCondiviso
     */
    public IdClient getSeeder(){
        if(this.seeders.isEmpty())
            return null;
        IdClient id =  this.seeders.removeFirst();
        this.seeders.addLast(id);
        return id;
    }

    /**
     * Restituisce il numero di seeders
     * @return il numero di seeders
     */
    public int sizeSeeder(){
        return this.seeders.size();
    }

    /**
     * Restituisce il numero di Leacher
     * @return il numero di Leacher
     */
    public int sizeLeacher(){
        return this.leachers.size();
    }

    /**
     * Controlla se id è seeder per questo FileCondiviso
     * @param id client da controllare
     * @return true se id è seeder, false altrimenti
     */
    public boolean containsSeeder(IdClient id){
        return this.seeders.contains(id);
    }

    /**
     * Controlla se id è leacher per questo FileCondiviso
     * @param id client da controllare
     * @return true se id è leacher, false altrimenti
     */
    public boolean containsLeacher(IdClient id){
        return this.leachers.contains(id);
    }

    /**
     * restituisce un iteratore per i seeders
     * @return l'iteratore
     */
    public Iterator scanSeeder(){
        return this.seeders.iterator();
    }

    /**
     * restituisce un iteratore per i leachers
     * @return l'iteratore
     */
    public Iterator scanLeacher(){
        return this.leachers.iterator();
    }

    /**
     * Stampa a video le informazioni dell'oggetto
     */
    public void stampa(){
        Iterator gen_seeders = scanSeeder();
        Iterator gen_leachers = scanLeacher();
        System.out.println("Seeders: ");
        while(gen_seeders.hasNext()){
            System.out.println(gen_seeders.next());
        }
        System.out.println("Leachers: ");
        while(gen_leachers.hasNext()){
            System.out.println(gen_leachers.next());
        }
    }
}
