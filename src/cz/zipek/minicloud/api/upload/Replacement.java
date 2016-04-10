/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.upload;

import cz.zipek.minicloud.api.File;

/**
 *
 * @author Kamen
 */
public class Replacement {
	private final java.io.File local;
	private final File remote;
	
	public Replacement(java.io.File local, File remote) {
		this.local = local;
		this.remote = remote;
	}

	/**
	 * @return the local
	 */
	public java.io.File getLocal() {
		return local;
	}

	/**
	 * @return the remote
	 */
	public File getRemote() {
		return remote;
	}
}
