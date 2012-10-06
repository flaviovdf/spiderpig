package br.ufmg.dcc.vod.ncrawler.stats;

import java.util.HashMap;
import java.util.Map.Entry;

import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;

public class StatsPrinter implements QueueProcessor<StatEvent> {

	private final HashMap<String, Integer> map;
	private final QueueService service;
	private QueueHandle h;
	private Display display;
	
	public StatsPrinter(QueueService service) {
		this.map = new HashMap<String, Integer>();
		this.h = service.createMessageQueue("Stats");
		this.service = service;
	}

	/**
	 * SHOULD BE CALLED BEFORE THREAD STARTS!
	 */
	public void setDisplay(Display display) {
		this.display = display;
	}

	public void start() {
		service.startProcessor(h, this);
	}
	
	@Override
	public String getName() {
		return "StatsPrinter";
	}

	public void notify(StatEvent se) {
		try {
			service.sendObjectToQueue(h, se);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void process(StatEvent se) {
		for (Entry<String, Integer> e : se.getIncrements().entrySet()) {
			Integer integer = map.get(e.getKey());
			if (integer == null) {
				integer = 0;
			}
			map.put(e.getKey(), integer + e.getValue());
		}
		
		System.out.println(se.getIncrements());
		display.print(map);
	}
}