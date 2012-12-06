package common;

import java.util.concurrent.TimeUnit;

/**
 * Classe contenente costanti di configurazione.
 * @author Federico Della Bona - Alessandro Lensi
 */
public class Configuration {
    /* Configuration del Gestore Trasmissione File */

    static public final int GTF_SOCKET_TIMEOUT = 1000;
    static public final int GTF_MAX_UPLOAD_THREADS = 5;
    static public final String GTF_TMP_DIR = "/tmp/";
    static public final long GTF_AWAIT_DOWNLOAD_TIMEOUT = 1;
    static public final TimeUnit GTF_AWAIT_DOWNLOAD_TIMEOUT_UNIT = TimeUnit.MINUTES;
    static public final long GTF_AWAIT_UPLOAD_TIMEOUT = GTF_AWAIT_DOWNLOAD_TIMEOUT;
    static public final TimeUnit GTF_AWAIT_UPLOAD_TIMEOUT_UNIT = GTF_AWAIT_DOWNLOAD_TIMEOUT_UNIT;
    /*Configuration del Server */
    public static final int SRV_DEFAULT_UDP_PORT = 5555;
    public static final int SRV_DEFAULT_RMI_PORT = 1099;
    public static final String SRV_SERVICE_NAME = "ByteTorrent";
    /*Configuration del Gestore Keep Alive */
    public static final int GKA_SOCKET_TIMEOUT = 700;
    public static final long GKA_KEEPALIVE_FREQUENCY = 1000;

    /* Multicast */
    public static final int GM_SOCKET_TIMEOUT = 700;
    public static final int GM_DEFAULT_MULTICAST_PORT = 9999;
    public static final String GM_DEFAULT_MULTICAST_GROUP = "226.226.226.226";

    /* Client */
    public static final String CLT_DEFAULT_SERVER_HOST ="localhost";
    public static final String CLT_COMMAND_FILENAME = "/comandi.txt";


    public static boolean isPort(int p) {

        return (p >= 1 && p <= 65536);

    }
}
