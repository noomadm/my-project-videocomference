package it.unipr.ce.dsg.s2p.example.msg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class PingMessage extends BasicMessage{
	public static final String MSG_PEER_PING = "peer_ping";
	
	public PingMessage(PeerDescriptor peerDesc){
		super(MSG_PEER_PING, new Payload(peerDesc));
	}
}
