/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.communication.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.communication.Statistics;


/**
 * Transceiver communicating with UDP packets via unicast.
 * 
 * @author AndreR
 */
public class UnicastTransceiverUDP implements ITransceiverUDP, IReceiverUDPObserver
{
	private static final Logger log = Logger.getLogger(UnicastTransceiverUDP.class.getName());
	
	private int dstPort = 0;
	private InetAddress destination = null;
	private DatagramSocket socket = null;
	
	private final ITransmitterUDP transmitter = new TransmitterUDP();
	private final ReceiverUDP receiver = new ReceiverUDP();
	
	private final List<ITransceiverUDPObserver> observers = new CopyOnWriteArrayList<>();
	
	
	@Override
	public void addObserver(final ITransceiverUDPObserver o)
	{
		observers.add(o);
	}
	
	
	@Override
	public void removeObserver(final ITransceiverUDPObserver o)
	{
		observers.remove(o);
	}
	
	
	@Override
	public void enqueueCommand(final ACommand cmd)
	{
		if (socket == null)
		{
			return;
		}
		
		notifyOutgoingCommand(cmd);
		
		transmitter.enqueueCommand(cmd);
	}
	
	
	@Override
	public void open()
	{
		close();
		
		try
		{
			socket = new DatagramSocket();
		} catch (final SocketException err)
		{
			log.error("Could not create UDP socket.", err);
			return;
		}
		
		try
		{
			socket.connect(destination, dstPort);
			
			receiver.setSocket(socket);
			
			transmitter.setSocket(socket);
			transmitter.setDestination(destination, dstPort);
		}
		
		catch (final SocketException err)
		{
			log.error("Could not connect to UDP socket: " + destination.getHostAddress() + ":" + dstPort, err);
			
			return;
		}
		
		catch (final IOException err)
		{
			log.error("Transmitter or receiver setup failed", err);
			
			return;
		}
		
		receiver.addObserver(this);
		
		transmitter.start();
		receiver.start();
	}
	
	
	@Override
	public void open(final String host, final int newPort)
	{
		close();
		
		try
		{
			destination = InetAddress.getByName(host);
			
			dstPort = newPort;
			
			open();
		}
		
		catch (final UnknownHostException err)
		{
			log.error("Could not resolve " + host, err);
		}
	}
	
	
	@Override
	public void close()
	{
		if (socket != null)
		{
			receiver.removeObserver(this);
			
			transmitter.stop();
			receiver.stop();
			
			socket.close();
			socket.disconnect();
			
			if (!socket.isClosed())
			{
				socket.close();
			}
			
			socket = null;
		}
	}
	
	
	@Override
	public void setNetworkInterface(final NetworkInterface network)
	{
		// hint not used
	}
	
	
	@Override
	public boolean isOpen()
	{
		return socket != null;
	}
	
	
	@Override
	public void onNewCommand(final ACommand cmd)
	{
		notifyIncommingCommand(cmd);
	}
	
	
	private void notifyIncommingCommand(final ACommand cmd)
	{
		synchronized (observers)
		{
			for (final ITransceiverUDPObserver observer : observers)
			{
				observer.onIncomingCommand(cmd);
			}
		}
	}
	
	
	private void notifyOutgoingCommand(final ACommand cmd)
	{
		synchronized (observers)
		{
			for (final ITransceiverUDPObserver observer : observers)
			{
				observer.onOutgoingCommand(cmd);
			}
		}
	}
	
	
	@Override
	public Statistics getReceiverStats()
	{
		return receiver.getStats();
	}
	
	
	@Override
	public Statistics getTransmitterStats()
	{
		return transmitter.getStats();
	}
	
	
	@Override
	public void setDestination(final String dstAddr, final int newPort)
	{
		boolean start = false;
		
		if (socket != null)
		{
			start = true;
			close();
		}
		
		try
		{
			destination = InetAddress.getByName(dstAddr);
			
			dstPort = newPort;
		}
		
		catch (final UnknownHostException e)
		{
			log.error("Unknown host: " + dstAddr, e);
		}
		
		if (start)
		{
			open(dstAddr, newPort);
		}
	}
}