package de.uni_stuttgart.ipvs.vs.ex6.replication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.uni_stuttgart.ipvs.vs.ex6.communication.MessageWithSource;
import de.uni_stuttgart.ipvs.vs.ex6.communication.ReadLockRequest;
import de.uni_stuttgart.ipvs.vs.ex6.communication.ReadRequest;
import de.uni_stuttgart.ipvs.vs.ex6.communication.UnlockRequest;
import de.uni_stuttgart.ipvs.vs.ex6.communication.Util;
import de.uni_stuttgart.ipvs.vs.ex6.communication.ValueResponse;
import de.uni_stuttgart.ipvs.vs.ex6.communication.VoteResponse;
import de.uni_stuttgart.ipvs.vs.ex6.communication.VoteResponse.Vote;
import de.uni_stuttgart.ipvs.vs.ex6.communication.WriteLockRequest;
import de.uni_stuttgart.ipvs.vs.ex6.communication.WriteRequest;

public class Replica<T> extends Thread {

	public enum LockType {
		UNLOCKED, READLOCK, WRITELOCK
	};

	private int id; // ID of this replica
	private int nVotes; // number of votes held by this replica
	private double availability; // availability of this replica
	private VersionedValue<T> value; // local copy of replicated value

	protected DatagramSocket socket;

	protected LockType lock;
	/**
	 * The field lockHolders indicates the addresses of the clients holding a lock.
	 * It should be empty if and only if the lock is set to UNLOCKED.
	 */
	protected Set<SocketAddress> lockHolders;

	public Replica(int id, int nVotes, double availability, T initialValue) throws SocketException {
		super("Replica:" + id);
		this.id = id;
		this.nVotes = nVotes;
		this.availability = availability;
		this.value = new VersionedValue<T>(0, initialValue);
		this.socket = new DatagramSocket();
		this.lock = LockType.UNLOCKED;
		this.lockHolders = new HashSet<SocketAddress>();
	}

	/**
	 * TODO
	 * 
	 * Part b) Implement the method run() of the Replica class to receive and
	 * process request messages ({@link ReadLockRequest}, {@link WriteLockRequest},
	 * {@link UnlockRequest}, {@link ReadRequest} and {@link WriteRequest}). To
	 * simulate a replica that is sometimes unavailable, it should randomly discard
	 * requests, but only as long as it is not locked. The probability for
	 * discarding a request is (1.0 - availability).
	 * 
	 * For each request received, it must also be checked whether the request is
	 * valid. For instance: Does the requesting client hold the correct lock? Is the
	 * replica unlocked when a new lock is requested?
	 */
	@Override
	public void run() {
		try {
			byte requestBuf[] = new byte[4096];
			DatagramPacket request = new DatagramPacket(requestBuf, requestBuf.length);
			socket.receive(request);
			SocketAddress source = request.getSocketAddress();
			@SuppressWarnings("unchecked")
			T value = (T) Util.datagramPacketToObject(request);

			MessageWithSource<T> message = new MessageWithSource<T>(source, value);
			String requestType = message.getMessage().getClass().getName();
			
			
			if (requestType.equals("ReadLockRequest")) {
				VoteResponse response = new VoteResponse(Vote.YES, id, nVotes);
				double event = Math.random();
				if (this.lock == LockType.READLOCK||event > this.availability) {
					response = new VoteResponse(Vote.NO, id, nVotes); 
				}
			byte responseBuf[] = Util.objectToByteArray(response);
			DatagramPacket datagram = new DatagramPacket(responseBuf,responseBuf.length,message.getSource());
		    socket.send(datagram);
			}
			// TODO: complete this method!
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}

	/**
	 * Send a {@link VoteResponse} with the specified vote and version. The number
	 * of votes is determined by the replica's nVotes field.
	 * 
	 * @param address
	 * @param vote
	 * @param version
	 * @throws IOException
	 */
	protected void sendVote(SocketAddress address, VoteResponse.Vote vote, int version) throws IOException {
		byte[] buf = Util.objectToByteArray(new VoteResponse(vote, version, nVotes));
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address);
		socket.send(packet);
	}

	/**
	 * Send a {@link ValueResponse} with the specified value.
	 * 
	 * @param address
	 * @param value
	 * @throws IOException
	 */
	protected void sendValue(SocketAddress address, T value) throws IOException {
		byte buf[] = Util.objectToByteArray(new ValueResponse<T>(value));
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address);
		socket.send(packet);
	}

	/**
	 * @return this replica's ID
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return this replica's number of votes
	 */
	public int getVotes() {
		return nVotes;
	}

	/**
	 * @return this replica's address
	 */
	public SocketAddress getSocketAddress() {
		return socket.getLocalSocketAddress();
	}

}
