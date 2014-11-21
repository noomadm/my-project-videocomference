package it.unipr.ce.dsg.s2p.sip;

/*
 * Copyright (C) 2010 University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Designer(s):
 * Marco Picone (picone@ce.unipr.it)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * Michele Amoretti (michele.amoretti@unipr.it)
 * 
 * Developer(s)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * 
 */

import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.RouteHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipMethods;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.MethodId;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

/**
 * The <code>Node</code> is an abstract class to send and receive 
 * Peer Message, held into SIP MESSAGE. <code>Node</code> manage also
 * SIP MESSAGE incoming from Session Border Controller. 
 * <p>
 * The abstract class <code>Node</code> implements the SipProviderListener and 
 * the TransactionClientListener, from MjSIP library, that listens incoming SIP message 
 * request and response. 
 * <code>Node</code> allow to the peer to send, through SipProvider,
 * own messages to other peer and Session Border Controller (SBC). 
 * 
 * @author Fabrizio Caramia
 *
 */


public abstract class Node implements SipProviderListener, TransactionClientListener {

	//private boolean requestPublicAddress = false;

	protected NodeConfig nodeConfig;

	protected SipProvider sipProvider;

	//protected SipKeepAlive keepAlive;

	/**
	 * When a new message is received from the remote peer
	 * 
	 * @param peerMsg the message of the peer
	 * @param sender the address of the sender peer
	 * @param contentType the content type
	 */
	protected abstract void onReceivedMsg(String peerMsg, Address sender, String contentType);

	/**
	 * When message sent from peer fails
	 * 
	 * @param peerMsgSended the message sent from sender peer
	 * @param receiver the address of the receiver peer
	 * @param contentType the content type
	 */
	protected abstract void onDeliveryMsgFailure(String peerMsgSended, Address receiver, String contentType);

	/**
	 * When message sent from peer successful
	 * 
	 * @param peerMsgSended message sent from sender peer
	 * @param receiver address of the receiver peer
	 * @param contentType the content type
	 */
	protected abstract void onDeliveryMsgSuccess(String peerMsgSended, Address receiver, String contentType);	

	/**
	 * Create a new Node
	 * 	
	 * @param file configuration file for node
	 */
	public Node(String file){

		this.sipProvider = new SipProvider(file);
		this.nodeConfig = new NodeConfig(file);

		//listen SIP Request
		startReceiveMessage();
	}

	/**
	 * Create a new Node
	 * 
	 * @param file configuration file for node
	 * @param name peer name
	 * @param port peer port (UDP for default communication)
	 */

	public Node(String file, String name, int port){				
		//for android compatibility
		SipStack.debug_level=0;
		SipStack.default_transport_protocols = new String[1];
		SipStack.default_transport_protocols[0] = "udp";
				
		this.sipProvider = new SipProvider("AUTO-CONFIGURATION", port);		
				
		if(file!=null)
			this.nodeConfig = new NodeConfig(file);
		else
			this.nodeConfig = new NodeConfig();

		this.nodeConfig.peer_name = name;

		//listen SIP Request
		startReceiveMessage();
	}

	/**
	 * Enables to listen incoming message
	 */
	protected void startReceiveMessage(){

		sipProvider.addSelectiveListener(SipProvider.ANY, this);
	}


	/**
	 * Disable the listener for incoming message
	 */
	protected void stopReceiveMessage(){

		sipProvider.removeSelectiveListener(new MethodId(SipMethods.MESSAGE));  
	}


	/**
	 * Send peer message through SIP MESSAGE using <code>toAddress</code> as destination address 
	 * 
	 * @param toAddress destination address
	 * @param fromAddress sender address 
	 * @param msg message message to send
	 * @param contentType type of content (es. application/json or application/xml)
	 */
	protected void sendMessage(Address toAddress, Address fromAddress, String msg, String contentType){

		sendMessage(toAddress, null, fromAddress, msg, contentType);

	}


	/**
	 * Send peer message through SIP MESSAGE using <code>toContactAddress</code> as destination address 
	 * 
	 * @param toAddress local destination address
	 * @param toContactAddress remote destination address
	 * @param fromAddress sender address 
	 * @param msg message message to send
	 * @param contentType contentType type of content (es. application/json or application/xml)
	 */
	protected void sendMessage(Address toAddress,  Address toContactAddress, Address fromAddress, String msg, String contentType){

		NameAddress destAddress = new NameAddress(toAddress);
		NameAddress senderAddress = new NameAddress(fromAddress);

		if(destAddress!=null && senderAddress!=null){

			if(contentType==null || contentType.equals("")) contentType="application/text";

			Message sipMessage = MessageFactory.createMessageRequest(sipProvider, destAddress, senderAddress, null, contentType, msg);

			if(toContactAddress!=null && (!toAddress.equals(toContactAddress)))
				sipMessage.addRouteHeader(new RouteHeader(new NameAddress(toContactAddress+";lr")) );

			TransactionClient tClient = new TransactionClient(sipProvider, sipMessage, this);
			tClient.request();

		}

	}

	/**
	 * Get peer Address (es. alice@pcdomain.it:5070 or alice@192.168.1.100.it:5070)
	 * 
	 * @return Address the address of peer
	 */

	protected Address getAddress(){

		SipURL result = null;
		if(sipProvider!=null)
			result = sipProvider.getContactAddress(nodeConfig.peer_name);

		return new Address(result);
	}
	

	/**
	 * From SipProviderListener
	 */
	
	public void onReceivedMessage(SipProvider sipPvd, Message sipMsg) {

		TransactionServer tServer=new TransactionServer(sipPvd, sipMsg, null);
		tServer.respondWith(MessageFactory.createResponse(sipMsg, 200, SipResponses.reasonOf(200), null));

		//For SIP Request with method MESSAGE
		if(sipMsg.isMessage())
		{						
			String contentType = sipMsg.getContentTypeHeader().getContentType();
			System.out.println("Nhan tin nhan");
			onReceivedMsg(sipMsg.getBody(), new Address(sipMsg.getFromHeader().getNameAddress().getAddress()),  contentType);	
		}


	}

	/**
	 * From TransactionClientListener
	 */
	public void onTransFailureResponse(TransactionClient tClient, Message sipMsg) {
		Message reqMessage = tClient.getRequestMessage();
		System.out.println("Phan hoi tin nhan that bai");
		onDeliveryMsgFailure(reqMessage.getBody(), new Address(reqMessage.getToHeader().getNameAddress().getAddress()),reqMessage.getContentTypeHeader().getContentType());
	}

	/**
	 * From TransactionClientListener
	 */
	public void onTransProvisionalResponse(TransactionClient tClient, Message sipMsg) {
		// TODO Auto-generated method stub

	}


	/**
	 * From TransactionClientListener. If <code>sipMsg</code> is a SIP message response 
	 * from Session Border Controller call <code>onReceivedSBCContactAddress()<code>
	 */

	public void onTransSuccessResponse(TransactionClient tClient, Message sipMsg) {
						
		Message reqMessage = tClient.getRequestMessage();
		System.out.println("Phan hoi tin nhan thanh cong");
		onDeliveryMsgSuccess(reqMessage.getBody(), new Address(reqMessage.getToHeader().getNameAddress().getAddress()), reqMessage.getContentTypeHeader().getContentType());
	
	}

	/**
	 * From TransactionClientListener
	 */

	public void onTransTimeout(TransactionClient tClient) {
		Message reqMessage = tClient.getRequestMessage();
		onDeliveryMsgFailure(reqMessage.getBody(), new Address(tClient.getRequestMessage().getToHeader().getNameAddress().getAddress()), reqMessage.getContentTypeHeader().getContentType());
	}


}
