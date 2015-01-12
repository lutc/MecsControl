package com.example.mecscontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;

import android.util.Log;

public class TelnetControl {
	public static final TelnetControl INSTANCE = new TelnetControl();
	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	private String address;
	private int port;
	private boolean IsConnected = false;

	public TelnetControl() {

	}

	public boolean Connect(String address, int port) {
		this.address = address;
		this.port = port;
		try {
			telnet.connect(address, port);

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("MECS", "Server does not respond " + e.getMessage());
			SetVariablesToNull();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("MECS", "Server does not respond " + e.getMessage());
			SetVariablesToNull();
			return false;
		}
		in = telnet.getInputStream();
		out = new PrintStream(telnet.getOutputStream());
		IsConnected = true;
		return true;
	}

	public boolean Reconnect() {
		try {
			try {
				telnet.disconnect();
			} catch (Exception e) {
				Log.e("MECS", "Disconnect Beda..." + e.getMessage());
			}

			telnet.connect(address, port);

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("MECS", "Server does not respond " + address + e.getMessage());
			SetVariablesToNull();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("MECS", "Server does not respond " + e.getMessage());
			SetVariablesToNull();
			return false;
		}
		in = telnet.getInputStream();
		out = new PrintStream(telnet.getOutputStream());
		IsConnected = true;
		return true;
	}

	public InputStream getIn() {
		return in;
	}

	public boolean GetIsConnected() {
		return telnet.isConnected();
		// return IsConnected;
	}

	private void SetVariablesToNull() {
		in = null;
		out = null;
		IsConnected = false;
	}

	public boolean Connect(String address) {
		return this.Connect(address, 4939);
	}

	public void SendCommand(String command) {
		if (out == null) {
			Log.e("MECS", "Couldn't connect");
			return;
		}
		out.println(command);
		out.flush();
	}

	public void Disconnect() {
		try {
			telnet.disconnect();
			SetVariablesToNull();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
