package common.remote;

import java.io.Serializable;
import common.identifier.FileDescriptor;
import common.identifier.IdClient;

/**
 * Contenitore di informazioni, serializzabile, che contiene un file
 * descriptor, e un seeder associato.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class SearchResult implements Serializable{
    private FileDescriptor file;
    private IdClient seeder;

    /**
     * Costruisce un nuovo risultato di ricerca.
     * @param file Il FileDescriptor del file.
     * @param seeder L'IdClient di un seeder di quel file.
     */
    public SearchResult(FileDescriptor file, IdClient seeder) {

        this.file = file;
        this.seeder = seeder;
    }

    /**
     * Metodo per estrarre il file descriptor.
     * @return Il FileDescriptor del file
     */
    public FileDescriptor getFileDescriptor(){
        return this.file;
    }

    /**
     * Metodo per estrarre il seeder
     * @return L'IdClient di un seeder del file.
     */
    public IdClient getSeeder(){
        return this.seeder;
    }

    

}
