package br.ufmg.dcc.vod.spiderpig.master.walker.monitor;

/**
 * This condition literally does nothing. By doing nothing, it will never notify
 * anyone that the crawl has finished.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class NeverEndingCondition extends AbstractStopCondition {

    @Override
    public void dispatched() {
    }

    @Override
    public void resultReceived() {
    }

    @Override
    public void errorReceived() {
    }

}
