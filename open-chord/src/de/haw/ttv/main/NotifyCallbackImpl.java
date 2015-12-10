package de.haw.ttv.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class NotifyCallbackImpl implements NotifyCallback {

	final List<BroadcastLog> broadcastLog = new ArrayList<>();
	final List<ID> uniquePlayers = new ArrayList<>();
	
	private ChordImpl chordImpl;
	
	public void setChordImpl(ChordImpl chordImpl){
		
	}
	
	@Override
	public void retrieved(ID target) {
		System.out.println(target.toString());
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		int transactionID = chordImpl.getLastSeenTransactionID();

		// chordImpl.setLastSeenTransactionID(transactionID);
		broadcastLog.add(new BroadcastLog(source, target, hit, transactionID));

		if (!uniquePlayers.contains(source)) {
            uniquePlayers.add(source);
        }
        Collections.sort(uniquePlayers);
		
	}

	public List<ID> getUniquePlayers() {
		return uniquePlayers;
	}

}
