package br.ufmg.dcc.vod.ncrawler.queue;

public class QueueHandle {

	private final int id;

	QueueHandle(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueueHandle other = (QueueHandle) obj;
		if (id != other.id)
			return false;
		return true;
	}
}