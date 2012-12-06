package server.keepalive;

import java.util.Timer;
import common.identifier.IdClient;

/**
 * Mantiene l'associazione tra il Task che deve eliminare il client
 * e il timer che lo schedula
 * @author Federico Della Bona - Alessandro Lensi
 */
public class TimerKiller {

    private KillClient killer;
    private Timer timer;

    /**
     * Crea un TimerKiller
     * @param killer il task da schedulare
     * @param t il timer che schedula killer
     */
    TimerKiller(KillClient killer, Timer t) {
        this.killer = killer;
        this.timer = t;
    }

    /**
     * resetta il Timer che deve schedulare il task killer
     */
    public void reset() {
        this.killer.cancel();
        IdClient id = this.killer.getId();
        this.timer.purge();
        this.killer = new KillClient(
                killer.getData(), killer.getGestore(), id);
        this.timer.schedule(killer, 3000);
    }
}
