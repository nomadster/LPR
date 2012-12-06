package common.identifier;

import java.io.Serializable;

/**
 * La classe FileDescriptor realizza un identificatore per i file, e
 * contiene nome e dimensione
 * @author Federico Della Bona - Alessandro Lensi
 */
public class FileDescriptor implements Serializable {

    private String name;
    private long size;

    /**
     * Crea un FileDescriptor per un file
     * @param name nome del file
     * @param size numero di byte del file
     */
    public FileDescriptor(String name, long size) {
        this.name = name;
        this.size = size;
    }

    /**
     * Restituisce il nome del file
     * @return nome del file
     */
    public String getName() {
        return this.name;
    }

    /**
     * Restituisce la dimensione del file
     * @return numero di byte del file
     */
    public long getSize() {
        return this.size;
    }

    /**
     * Restituisce la stringa che rappresenta il FileDescriptor
     * @return la stringa che rappresenta il FileDescriptor
     */
    @Override
    public String toString() {
        return this.name + ":" + this.size;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() == FileDescriptor.class) {
            return this.toString().equals(((FileDescriptor) o).toString());
        }
        return false;

    }
}
