package br.ufmg.dcc.vod.ncrawler.distributed.nio.service;

import java.util.concurrent.SynchronousQueue;

public class KeepAliveThread extends Thread {

	private SynchronousQueue<Object> queue;

	public KeepAliveThread() {
		this.queue = new SynchronousQueue<>();
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				this.queue.take();
				break;
			} catch (InterruptedException e) {
			}
		}
	}

	public void poison() {
		try {
			this.queue.put(new Object());
		} catch (InterruptedException e) {
		}
	}
}