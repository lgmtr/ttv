package de.haw.ttv.main;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;

public class Main {

	private static final int S = 10; // number of ships
	public static final int I = 100; // number of mySectors
	private static final int WAITING_TIME_CHORD_JOIN = 5000;
//	private static final int MIDDLE = 2;
	private static final ID BIGGEST_ID = new ID(BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE).toByteArray());

	public ID myID;
	private static Chord chord;
	public ID[] mySectors = new ID[I];
	final boolean[] ships = new boolean[I];

	public static void main(String[] args) throws InterruptedException {
		Main main = new Main();
		main.initGame(main);
		Thread.sleep(100);
		main.startGame();
	}

	private void startGame() {
		System.out.println("MyID: " + chord.getChordImpl().getID().toString());
		Scanner scanner = new Scanner(System.in);
		// start the game after s was typed in
		String input;
		do {
			System.out.print("type \"s\" to begin: ");
			input = scanner.next();
		} while (!input.equals("s"));
		scanner.close();

		// waits 5 sec to make sure others have joined chord
		try {
			Thread.sleep(WAITING_TIME_CHORD_JOIN);
		} catch (InterruptedException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}

		myID = chord.getChordImpl().getID();
		mySectors = calculateSectors(chord.getChordImpl().getPredecessorID(), myID);
		setShips();

		// adds us to the unicePlayer sectors
		chord.getNotifyCallbackImpl().uniquePlayers.add(myID);

		// add the fingertables to
		Set<Node> fingerSet = new HashSet<>(chord.getChordImpl().getFingerTable());
		for (Node n : fingerSet) {
			chord.getNotifyCallbackImpl().uniquePlayers.add(n.getNodeID());
		}
		Collections.sort(chord.getNotifyCallbackImpl().uniquePlayers);

		// we start the game if we have the BIGGESTID
		if (chord.getChordImpl().getPredecessorID().compareTo(chord.getChordImpl().getID()) > 0) {
			System.out.println("I start!");

			chord.getNotifyCallbackImpl().calculateUniquePlayersSectors();
            chord.getNotifyCallbackImpl().calculateShootableSectors();
			shoot();
		}
	}

	public ID[] calculateSectors(ID from, ID to) {
		ID[] result = new ID[I];
		ID distance;

		// predecessorID might be bigger than our ID, due to Chord circle
		if (from.compareTo(to) < 0) {
			distance = to.subtract(from);
		} else {
			distance = BIGGEST_ID.subtract(from).add(to);
		}

		ID step = distance.divide(I);

		for (int i = 0; i < I; i++) {
			// (from + 1 + (i * step)) % biggestID
			result[i] = from.add(1).add(step.multiply(i)).mod(BIGGEST_ID);

			// System.out.println("sector " + (i + 1) + ": " + result[i]);
		}
		return result;
	}

	private void setShips() {
		Random rnd = new Random();
		int random;

		for (int i = 0; i < S; i++) {
			do {
				random = rnd.nextInt(I);
			} while (ships[random] == true);

			ships[random] = true;
		}
	}

	private void initGame(Main main) {
		chord = new Chord(main);
		chord.init();
	}
	
	/**
     * Checks if the target is inside the Sector boundaries.
     *
     * @param target target ID
     * @param sector Array of IDs
     * @return
     */
    public int isInSector(ID target, ID[] sector) {
        if (!target.isInInterval(sector[0], sector[sector.length - 1])) {
            return -1;
        }

        for (int i = 0; i < sector.length - 1; i++) {
            if (target.compareTo(sector[i]) >= 0 && target.compareTo(sector[i + 1]) < 0) {
                return i;
            }
        }

        if (target.compareTo(sector[sector.length - 1]) >= 0 && target.compareTo(findNextPlayer(target)) < 0) {
            return sector.length - 1;
        }

        return -1;
    }

    /**
     * returns the ID of the next player
     *
     * @param target
     * @return
     */
    public ID findNextPlayer(ID target) {
        List<ID> uniquePlayers = chord.getNotifyCallbackImpl().uniquePlayers;
        Collections.sort(uniquePlayers);

        for (int i = 0; i < uniquePlayers.size() - 1; i++) {
            if (target.isInInterval(uniquePlayers.get(i), uniquePlayers.get(i + 1))) {
                return uniquePlayers.get(i + 1);
            }
        }
        return uniquePlayers.get(0);
    }

	public void shoot() {
		System.out.println();
		Random rnd = new Random();

		List<ID> shootableSectors = chord.getNotifyCallbackImpl().shootableSectors;

		int sectorNumber = rnd.nextInt(shootableSectors.size());
		ID target = shootableSectors.get(sectorNumber);

		ID middleOfSector = target.add(10).mod(BIGGEST_ID);

		System.out.println("shooting at: " + target.toBigInteger());
		ShootThread st = new ShootThread(chord.getChordImpl(), middleOfSector);
		st.start();

	}

}
