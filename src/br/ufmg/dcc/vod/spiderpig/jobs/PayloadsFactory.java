package br.ufmg.dcc.vod.spiderpig.jobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.protobuf.ByteString;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload.Builder;

public class PayloadsFactory {

    private Map<String, byte[]> filesToSave;

    public PayloadsFactory() {
        this.filesToSave = new HashMap<String, byte[]>();
    }

    public PayloadsFactory addPayload(CrawlID crawlID, byte[] payload) {
        return addPayload(crawlID, payload, "");
    }
    
    public PayloadsFactory addPayload(CrawlID crawlID, byte[] payload, 
            String suffix) {
        String fileName = crawlID.getResourceType() + "-" + crawlID.getId() + 
                "-" + suffix;
        addPayload(fileName, payload);
        return this;
    }
    
    public PayloadsFactory addPayload(String fileName, byte[] payload) {
        this.filesToSave.put(fileName, payload);
        return this;
    }
    
    public Collection<Payload> build() {
        Collection<Payload> payloads = new ArrayList<>();
        Builder builder = Payload.newBuilder();
        for (Entry<String, byte[]> entry : this.filesToSave.entrySet()) {
            builder.setPayloadFileName(entry.getKey());
            builder.setPayloadFile(ByteString.copyFrom(entry.getValue()));
            payloads.add(builder.build());
        }
        return payloads;
    }
    
}
