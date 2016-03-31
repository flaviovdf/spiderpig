package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class IDStruct {

    private final List<CrawlID> reverseLinks;
    private final CrawlID id;
    
    private List<CrawlID> links;
    private Set<CrawlID> waitingLinks;
    private boolean walkStep;
    
    IDStruct(CrawlID id) {
        this.id = id;
        this.reverseLinks = Lists.newArrayList();
        this.links = null;
        this.waitingLinks = null;
        this.walkStep = false;
    }
    
    public List<CrawlID> getReverseLinks() {
        return Lists.newCopyOnWriteArrayList(reverseLinks);
    }
    
    public List<CrawlID> getLinks() {
        return Lists.newCopyOnWriteArrayList(links);
    }
    
    public Set<CrawlID> getWaitingLinks() {
        return Sets.newCopyOnWriteArraySet(waitingLinks);
    }
    
    public void initLinks(Iterable<CrawlID> links) {
        Preconditions.checkNotNull(links);
        
        if (this.links == null) {
            this.links = Lists.newArrayList(links);
            this.waitingLinks = Sets.newHashSet(links);
        }
    }

    public boolean areLinksSet() {
        return this.links != null;
    }
    
    public boolean isDoneWaiting() {
        return this.waitingLinks != null &&
                this.waitingLinks.size() == 0;
    }
    
    public boolean addReverseLink(CrawlID id) {
        return this.reverseLinks.add(id);
    }

    public boolean markLinkAsDone(CrawlID id) {
        if (this.waitingLinks == null)
            return false;
        
        return this.waitingLinks.remove(id);
    }

    public CrawlID getID() {
        return this.id;
    }

    public boolean isWalkStep() {
        return walkStep;
    }
    
    public void setWalkStepTrue() {
        this.walkStep = true;
    }
}
