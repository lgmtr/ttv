package de.haw.ttv.main;

import java.net.MalformedURLException;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Chord {

    private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    private static final String HOST_IP = "192.168.99.99";
    private static final String HOST_PORT = "8080";
    private static final String JOIN_IP = "192.168.99.225";
    private static final String JOIN_PORT = "8181";
    private static final String MODUS = "join"; // "join" and "create" are valid
    
    private ChordImpl chordImpl;
    
    private NotifyCallbackImpl notifyCallbackImpl;
    
    public Chord() {
		// TODO Auto-generated constructor stub
	}
    
    public void init() {
        if (MODUS.equals("join")) {
            joinChord();
        } else if (MODUS.equals("create")) {
            hostChord();
        } else {
            System.out.println("ERROR: choose if you want to be server or client!");
        }
    }

	private void joinChord() {
		try{
			PropertiesLoader.loadPropertyFile();
		}catch(IllegalStateException e){
			System.err.println(e.getMessage());
		}
		
		URL localURL = null;
        try {
            localURL = new URL(PROTOCOL + "://" + JOIN_IP + ":" + JOIN_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        setChordImpl(new ChordImpl());
		notifyCallbackImpl = new NotifyCallbackImpl();
		notifyCallbackImpl.setChordImpl(chordImpl);
		chordImpl.setCallback(notifyCallbackImpl);

        URL bootstrapURL = null;
        try {
            bootstrapURL = new URL(PROTOCOL + "://" + HOST_IP + ":" + HOST_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            chordImpl.join(localURL, bootstrapURL);
        } catch (ServiceException e) {
            throw new RuntimeException("Could not join DHT!", e);
        }

        System.out.println("Chord running on: " + localURL);
		
	}

	private void hostChord() {
		try{
			PropertiesLoader.loadPropertyFile();
		}catch(IllegalStateException e){
			System.err.println(e.getMessage());
		}

        URL localURL = null;
        try {
            localURL = new URL(PROTOCOL + "://" + HOST_IP + ":" + HOST_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
		setChordImpl(new ChordImpl());
		notifyCallbackImpl = new NotifyCallbackImpl();
		notifyCallbackImpl.setChordImpl(chordImpl);
		chordImpl.setCallback(notifyCallbackImpl);

        try {
            chordImpl.create(localURL);
        } catch (ServiceException e) {
            throw new RuntimeException("Could not create DHT!", e);
        }

        System.out.println("Chord listens on: " + localURL);
	}

	public ChordImpl getChordImpl() {
		return chordImpl;
	}

	public void setChordImpl(ChordImpl chordImpl) {
		this.chordImpl = chordImpl;
	}

	public NotifyCallbackImpl getNotifyCallbackImpl() {
		return notifyCallbackImpl;
	}
	
}
