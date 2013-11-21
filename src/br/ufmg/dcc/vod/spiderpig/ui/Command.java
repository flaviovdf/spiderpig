package br.ufmg.dcc.vod.spiderpig.ui;

import br.ufmg.dcc.vod.spiderpig.common.config.Configurable;

public interface Command extends Configurable {

    public void exec() throws Exception;
    
}