spiderpig
---------

A customizable distributed web crawler taylored for researching online social 
networks and social media content.

Basically, I use this for my data collecting needs. It is ever-evolving. 
Moreover, I use this package to keep up with some programming techniques such
as new API's, distributed patterns etc. Thus from time to time I may perform 
some major refactorings on the package.

This package contains the common code for:

    * Distributed crawling
        * Event based communication
        * Failure detection

    * Journal Recovery
        * Journal is implemented, still needs recovery code

    * Different Schedulers
        * BFS (Snowball)
           * Greedy attempt of crawling everything
        * Random Walks
           * Limited by a maximum number of steps
           * Damping factor (stop probability)
           * Doing random jumps is hard on online social networks. Some id
             generation is necessary. It is easy to extend the crawler for this.
        * EGO Networks crawls 
           * (neighbors, neighbors of neighbors, neighbors of neighbors of neighbors, etc)
           * Basically, a pruned BFS

    * Caching
        * Some walkers revist node (such as random walk). Caching aliviates requests to servers in these cases

To really crawl content individual *Jobs* should be implemented.


dependecies
-----------

    * guava (Google's common Java library) 13.0+
    * protocol-buffers (Google's protocol buffer library and compiler) 2.4+
    * commons-cli (Apache commons command line interface library) 1.2.x
    * commons-configuration (Apache commons command configuration library) 1.6
    * commons-lang (Apache commons lang library) 2.6
    * commons-logging (Apache commons loggin library) 1.1.1
    * log4j (Apache's loggind library) 1.2.x
    * junit4 (For unit testing) 4.0+

If you have ant (ant-optional on ubuntu) and ivy (ivy on ubuntu) dependecies 
will be autodownloaded. Just run *ant deps*.