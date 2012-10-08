vod-crawler
-----------

A customizable distributed web crawler taylored for researching online social 
networks and social media content.

Basically, I use this for my data collecting needs. It is ever-evolving.

*THIS PACKAGE IS NOT READY FOR PRODUCTION USE. IT MAY NEVER BE*

This package contains the common code for:

    * Distributed crawling
        * Event based communication
        * Failure detection (under work)

    * Journal Recovery
        * Journal is implemented, still needs recovery code

    * Different Schedulers
        * Only BFS (Snowball) currently
        * Others soon
            * Random Walks
            * Metropolis Hasting

To really crawl content individual *Jobs* should be implemented.

dependecies
-----------

    * guava (Google's common Java library) 13.0+
    * commons-cli (Apache commons command line interface library) 1.2.x
    * log4j (Apache's loggind library) 1.2.x
    * junit4 (For unit testing) 4.0+

If you have ant (ant-optional on ubuntu) and ivy (ivy on ubuntu) dependecies will
be autodownloaded.