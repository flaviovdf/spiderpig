package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import br.ufmg.dcc.vod.spiderpig.common.Constants;

public class WorkerManagerException extends RuntimeException {

    private static final long serialVersionUID = Constants.SERIAL_UID;

    public WorkerManagerException(String msg) {
        super(msg);
    }
}
