package br.ufmg.dcc.vod.ncrawler.stats;

import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.actor.AbstractActor;

public class StatsActor extends AbstractActor<StatUpdateMessage> {

	private QueueHandle handle;
	private Display display;
	
	public StatsActor(QueueService service) {
		super(1, service);
		this.handle = service.createMessageQueue("Stats");
	}

	/**
	 * SHOULD BE CALLED BEFORE THREAD STARTS!
	 */
	public void setDisplay(Display display) {
		this.display = display;
	}

	@Override
	public String getName() {
		return "StatsPrinter";
	}

	@Override
	public void process(StatUpdateMessage sum) {
		display.print(sum.getMap());
	}

	@Override
	public QueueHandle getQueueHandle() {
		return this.handle;
	}
}