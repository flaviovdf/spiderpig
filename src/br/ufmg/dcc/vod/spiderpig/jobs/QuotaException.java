package br.ufmg.dcc.vod.spiderpig.jobs;

import java.io.IOException;

import br.ufmg.dcc.vod.spiderpig.common.Constants;

/**
 * Thrown when request quotas to a service is exceeded.
 */
public class QuotaException extends IOException {

    private static final long serialVersionUID = Constants.SERIAL_UID;
    
    public QuotaException(Exception cause) {
        super(cause);
    }
}