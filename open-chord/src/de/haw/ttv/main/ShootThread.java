package de.haw.ttv.main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;

public class ShootThread extends Thread {

	private final ID target;
    private final Chord chord;
	
    public ShootThread(Chord chord, ID target) {
        this.chord = chord;
        this.target = target;
    }
	
	@Override
    public void run() {
        synchronized (this) {
            try {
                this.wait(100);
                chord.retrieve(target);
            } catch (ServiceException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
