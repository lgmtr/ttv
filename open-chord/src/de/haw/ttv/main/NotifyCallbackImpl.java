package de.haw.ttv.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class NotifyCallbackImpl implements NotifyCallback {

	final List<BroadcastLog> broadcastLog = new ArrayList<>();
	final List<ID> uniquePlayers = new ArrayList<>();
	final Map<ID, ID[]> uniquePlayersSectors = new HashMap<>();
    List<ID> shootableSectors = new ArrayList<>();
	
    //private members
    private Main main = null;
    private ChordImpl chordImpl = null;
    private int shipsLeft = 10;
    private final Map<ID, Integer> hitForID = new HashMap<>();

	public void setChordImpl(Main main, ChordImpl chordImpl) {
		this.main = main;
        this.chordImpl = chordImpl;
	}

	@Override
	public void retrieved(ID target) {
		handleHit(target);
        calculateUniquePlayersSectors();
        calculateShootableSectors();
        main.shoot();
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		int transactionID = chordImpl.getLastSeenTransactionID();

        // chordImpl.setLastSeenTransactionID(transactionID);
        broadcastLog.add(new BroadcastLog(source, target, hit, transactionID));

        if (hit) {
            if (hitForID.containsKey(source)) {
                int tmp = hitForID.get(source);
                hitForID.put(source, ++tmp);

                if (tmp == 10) {
                    System.out.println("Player " + target + " lost!\nlast seen transaction ID: " + chordImpl.getLastSeenTransactionID());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NotifyCallbackImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                hitForID.put(source, 1);
            }
        }

        if (!uniquePlayers.contains(source)) {
            uniquePlayers.add(source);
        }
        Collections.sort(uniquePlayers);
	}

	public List<ID> getUniquePlayers() {
		return uniquePlayers;
	}
	
	private void handleHit(ID target) {
        ID[] sectors = main.mySectors;
        System.out.println("Ships left: " + shipsLeft);
        for (int i = 0; i < sectors.length - 1; i++) {
            if (target.compareTo(sectors[i]) >= 0 && target.compareTo(sectors[i + 1]) < 0) {
                if (main.ships[i]) {
                    System.out.println("Ship " + shipsLeft + " destroyed in sector " + (i + 1));
                    shipsLeft--;
                    main.ships[i] = false;
                    chordImpl.broadcast(target, Boolean.TRUE);
                    break;
                } else {
                    System.out.println("no Ship" + " in sector " + (i + 1));
                    chordImpl.broadcast(target, Boolean.FALSE);
                    break;
                }
            }
        }

        if (target.compareTo(sectors[sectors.length - 1]) >= 0 && target.compareTo(main.myID) <= 0) {
            if (main.ships[sectors.length - 1]) {
                System.out.println("Ship " + shipsLeft + " destroyed in sector 100");
                shipsLeft--;
                main.ships[sectors.length - 1] = false;
                chordImpl.broadcast(target, Boolean.TRUE);
            } else {
                System.out.println("no Ship" + " in sector 100");
                chordImpl.broadcast(target, Boolean.FALSE);
            }
        }

        if (shipsLeft < 1) {
            System.out.println("I LOST!");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
	
	/**
     * calculates the 100 sectors for every player, if it has changed since it was last called
     */
    void calculateUniquePlayersSectors() {
        if (uniquePlayers.size() == uniquePlayersSectors.size()) {
            return;
        }

        for (int i = 0; i < uniquePlayers.size() - 1; i++) {
            ID[] newSectors = main.calculateSectors(
                    uniquePlayers.get(i), uniquePlayers.get(i + 1));
            uniquePlayersSectors.put(uniquePlayers.get(i), newSectors);
        }

        // case for the last player
        ID[] newSectors = main.calculateSectors(
                uniquePlayers.get(uniquePlayers.size() - 1), uniquePlayers.get(0));
        uniquePlayersSectors.put(uniquePlayers.get(uniquePlayers.size() - 1), newSectors);
    }

    /**
     * regenerates the ArrayList of potential target sectors
     */
    void calculateShootableSectors() {

        shootableSectors = new ArrayList<>();

        // fill List
        for (ID uniquePlayer : uniquePlayers) {
            for (int j = 0; j < Main.I; j++) {
                shootableSectors.add(uniquePlayersSectors.get(uniquePlayer)[j]);
            }
        }

        // remove fields, that are destroyed
        for (BroadcastLog bl : broadcastLog.toArray(new BroadcastLog[0])) {
            for (ID uniquePlayer : uniquePlayers) {
                ID[] sectors = uniquePlayersSectors.get(uniquePlayer);
                int index = main.isInSector(bl.getTarget(), sectors);
                if (index != -1) {
                    shootableSectors.remove(sectors[index]);
                }
            }
        }

        // remove own fielde
        for (ID id : main.mySectors) {
            shootableSectors.remove(id);
        }

        System.out.println("Number of shootable sectors: " + shootableSectors.size());
    }

}
