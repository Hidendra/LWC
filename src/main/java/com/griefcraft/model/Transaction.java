/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.model;

import java.util.Arrays;
import java.util.List;

public class Transaction {

	public enum Status {
		
		/**
		 * The protection is still active in the world and can be reversed
		 */
		ACTIVE,
		
		/**
		 * For some reason the transaction is now inactive. Most likely the
		 * protection was removed by the player
		 */
		INACTIVE
		
	}
	
	/**
	 * Transaction id in the database
	 */
	private int transactionId;
	
	/**
	 * Affected protection id
	 */
	private int protectionId;
	
	/**
	 * The status of the transaction
	 */
	private Status status;
	
	/**
	 * Metadata about the transaction. An example of one entry would be
	 * for iConomy prices to be pushed in here. Any module can modify the
	 * meta data and add their own data about the transaction.
	 */
	private String[] metadata;
	
	public Transaction() {
		// set some defaults to account for stupidness
		status = Status.INACTIVE;
		metadata = new String[0];
	}
	
	/**
	 * Add a string of data to the stored metadata
	 * 
	 * @param data
	 */
	public void addMetaData(String data) {
		String[] temp = new String[metadata.length + 1];
		System.arraycopy(metadata, 0, temp, 0, metadata.length);
		
		// push the data to the end of the temporary array
		// array.length doesn't start at 0, so we can be sure this is valid
		temp[metadata.length] = data;
		
		// we're okey
		this.metadata = temp;
	}
	
	/**
	 * Remove a string of known data from the stored metadata
	 * 
	 * @param data
	 * @return true if the given metadata was successfully removed
	 */
	public boolean removeMetaData(String data) {
		// sorry
		List<String> temp = Arrays.asList(metadata);
		int expected = metadata.length - 1;
		
		temp.remove(data);
		
		// that went better than expected
		this.metadata = temp.toArray(new String[temp.size()]);
		
		return metadata.length == expected;
	}
	
	public int getTransactionId() {
		return transactionId;
	}
	
	public int getProtectionId() {
		return protectionId;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public String[] getMetaData() {
		return metadata;
	}
	
	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	
	public void setProtectionId(int protectionId) { 
		this.protectionId = protectionId;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setMetaData(String[] metadata) {
		this.metadata = metadata;
	}
	
}
