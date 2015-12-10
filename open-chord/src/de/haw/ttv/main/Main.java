package de.haw.ttv.main;

import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;

public class Main {

	private static final int S = 10; // number of ships
    static final int I = 100; // number of mySectors
    private static final int WAITING_TIME_CHORD_JOIN = 5000;
    private static final int MIDDLE = 2;
	
    private ID myID;
    private Chord chord;
    
	public static void main(String[] args) {
		Main main = new Main();
		main.startGame();
	}

	
	private void startGame() {
		chord = new Chord();
		chord.init();
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

            shoot();
        }
	}

	private void shoot() {
		boolean player = false;
		ID uniquePlayer = null;
		do{
			int number = ThreadLocalRandom.current().nextInt(0, chord.getNotifyCallbackImpl().getUniquePlayers().size());
			uniquePlayer = chord.getNotifyCallbackImpl().getUniquePlayers().get(number);
			if(uniquePlayer.compareTo(myID)!=0)
				player = true;
			
			System.out.println("shoot schleife");
		}while(!player);
		ShootThread st = new ShootThread(chord.getChordImpl(), uniquePlayer);
		st.start();
		System.out.println("geschossen");
	}

}
