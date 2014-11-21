package com.example.demopeerpeer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private EditText addressEdit;
	private Button sendBut;
	private TextView addressPeer;
	private Button clearBut;
	private TextView contactAddressPeer;
	private Button exitBut;
	
	public static Handler handler = new Handler();
	
	private SimplePeer peer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addressPeer = (TextView)findViewById(R.id.addressPeer);
		contactAddressPeer = (TextView)findViewById(R.id.contAddressPeer);
		addressEdit = (EditText)findViewById(R.id.addressEditor);
		
		sendBut = (Button)findViewById(R.id.sendPing);
		clearBut = (Button)findViewById(R.id.clearButton);
		exitBut = (Button)findViewById(R.id.closeApp);
		
		sendBut.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {

				if(peer!=null) {
					if(addressEdit.getText().toString().contentEquals("")){
						Toast toast = Toast.makeText(getApplicationContext(),
								"Please type a Peer address (ex. bob@192.168.1.100:5070)" ,
								Toast.LENGTH_LONG);
	         		 	toast.show();	
					}
					else{						
						peer.pingToPeer(addressEdit.getText().toString());							
					}	
				}else{
					Toast toast = Toast.makeText(getApplicationContext(),
							"peer is null" ,
							Toast.LENGTH_LONG);
         		 	toast.show();	
				}
			}
		});
		
		clearBut.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {

				addressEdit.setText("");

			}
		});
		
		exitBut.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				peer.halt();
				finish();

			}
		});
	}

	@Override
	protected void onStart(){
		super.onStart();
		// Cai dat peer
		if(peer == null){
			peer = new SimplePeer(null, "4654amv65d4as4d65a4", "peerDroid", 5060);
			peer.setPeerActivity(this);
			
			addressPeer.setText("Address: " + peer.getAddressPeer());
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		addressPeer.setText("Address: " + peer.getAddressPeer());
		contactAddressPeer.setText("Contact Address: " +
				peer.getContactAddressPeer());
	}
	
}
