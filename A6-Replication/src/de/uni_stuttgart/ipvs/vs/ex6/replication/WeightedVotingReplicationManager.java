package de.uni_stuttgart.ipvs.vs.ex6.replication;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import de.uni_stuttgart.ipvs.vs.ex6.communication.MessageWithSource;
import de.uni_stuttgart.ipvs.vs.ex6.communication.VoteResponse;
import de.uni_stuttgart.ipvs.vs.ex6.communication.VoteResponse.Vote;

public class WeightedVotingReplicationManager<T> {

	protected Collection<Replica<T>> replicas;
	protected Collection<SocketAddress> replicaAddresses;

	protected int readThreshold;
	protected int writeThreshold;
	
	/**
	 * TODO
	 * 
	 * Part a) Complete the constructor by initializing read and write thresholds.
	 * You may choose arbitrary values as long as they satisfy the conditions for
	 * Weighted Voting.
	 * 
	 * Construct a WeightedVotingReplicationManager, determine read and write
	 * thresholds based on provided replicas and their numbers of votes
	 * 
	 * @param replicas
	 *            the set of replicas to manage for a given replicated value
	 */
	
	public WeightedVotingReplicationManager(Collection<Replica<T>> replicas) {
		this.replicas = replicas;
        int totalVotes = 0;
		replicaAddresses = new Vector<SocketAddress>(replicas.size());
		for (Replica<T> replica : replicas)
		{
			replicaAddresses.add(replica.getSocketAddress());
			totalVotes += replica.getVotes();
		}
		this.readThreshold = totalVotes/2;
		this.writeThreshold = totalVotes/2 + 1;
	}

	/**
	 * @return unmodifiable view on the SocketAddresses of all replica
	 */
	public Collection<SocketAddress> getReplicaAddresses() {
		return Collections.unmodifiableCollection(replicaAddresses);
	}

	/**
	 * @return number of managed replicas
	 */
	public int getNumberOfReplicas() {
		return replicas.size();
	}

	public int getReadThreshold() {
		return readThreshold;
	}

	public int getWriteThreshold() {
		return writeThreshold;
	}

	/**
	 * TODO
	 * 
	 * Part a) Implement the method checkQuorum to check whether a sufficient number
	 * of positive votes were received. If a sufficient number was received, this
	 * method should return the replies from the locked replicas (i.e., those that
	 * voted YES). Otherwise, a {@link QuorumNotReachedException} must be thrown.
	 * 
	 * This method is used by {@link checkReadQuorum} and {@link checkWriteQuorum}.
	 * 
	 * @throws QuorumNotReachedException
	 * @param replies
	 *            collection of received {@link VoteResponse} messages (wrapped in
	 *            {@link MessageWithSource})
	 * @param threshold
	 *            number of votes that are required to reach the quorum
	 */
	protected Collection<MessageWithSource<VoteResponse>> checkQuorum(
			Collection<MessageWithSource<VoteResponse>> replies, int threshold) throws QuorumNotReachedException {
		Collection<MessageWithSource<VoteResponse>> postiveReplies = new ArrayList<MessageWithSource<VoteResponse>>(); 
		int postiveVotes=0;
		for (MessageWithSource<VoteResponse> replie: replies) {
			if (replie.getMessage().getState() == Vote.YES) {
				postiveReplies.add(replie);
				postiveVotes++;
			}			
		}
		QuorumNotReachedException e =new QuorumNotReachedException(threshold, postiveVotes, replicaAddresses);
		if (postiveVotes < threshold) {throw e; }
		return postiveReplies;
	}

	// Uses checkQuorum to compare received positive votes against readThreshold
	public Collection<MessageWithSource<VoteResponse>> checkReadQuorum(
			Collection<MessageWithSource<VoteResponse>> replies) throws QuorumNotReachedException {
		return checkQuorum(replies, readThreshold);
	}

	// Uses checkQuorum to compare received positive votes against writeThreshold
	public Collection<MessageWithSource<VoteResponse>> checkWriteQuorum(
			Collection<MessageWithSource<VoteResponse>> replies) throws QuorumNotReachedException {
		return checkQuorum(replies, writeThreshold);
	}

}
