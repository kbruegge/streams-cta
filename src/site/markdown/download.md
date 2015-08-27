Download
========

The *streams* library is available as Maven dependency in the global
Apache software repository. Please refer to the [Maven artifact](dev/maven.html)
page for details.

In addition, the *stream-runner* is provided, which is a self-contained
executable Java archive that includes the basic *streams* API and the
runtime classes. In addition it provides the *streams-plotter* package,
which allows for live plots of data.

This *stream-runner* can be used to start stream processes right away:

       # java -jar stream-runner-0.9.11.jar experiment-file.xml


The *stream-runner* can be downloaded from

<div class="download" style="margin:auto; height: 40px; padding: 10px; margin-left: 20px;">
     <a href="http://download.jwall.org/streams/stream-runner-0.9.11.jar">
     <img src="./images/download-icon.png" style="float: left; vertical-align: middle;" />
     </a>
     <div style="float: left; margin-left: 10px;">
       <div>
        <a href="http://download.jwall.org/streams/stream-runner-0.9.11.jar">stream-runner</a>,
        Version 0.9.11
       </div>
       <div style="font-size: -2;">6.1 MB, MD5 checksum: <code>741c340f7e3afdfee54c8dcf4da0c884</code></div>
     </div>
</div>


Debian/Ubuntu
-------------

For ease of use we provide a Debian installer package. This package requires a moderate Java Runtime
environment to be installed (openjdk, Sun Java) of at least version 6 or later.

The package is available at:

<div class="download" style="margin:auto; height: 40px; padding: 10px; margin-left: 20px;">
     <a href="http://download.jwall.org/debian/pool/main/s/streams/streams_0.9.11-2_all.deb">
     <img src="./images/download-icon.png" style="float: left; vertical-align: middle;" />
     </a> 
     <div style="float: left; margin-left: 10px;">
       <div>
        <a href="http://download.jwall.org/debian/pool/main/s/streams/streams_0.9.11-2_all.deb">streams-0.9.11-2_all.deb</a>,
        Version 0.9.11-2
       </div>
       <div style="font-size: -2;">5.4 MB, MD5 checksum: <code>7089cebc0313bc6f872bed29cd46c5d6</code></div>
     </div> 
</div>


To install the package simply use the `dpkg` packet management of your Debian/Ubuntu system:

       # sudo dpkg -i streams-0.9.11-2_all.deb

This will install the complete runtime files onto your system and provides a
convenient `stream.run` command to run stream processes:

       # stream.run your-process.xml

To extend your stream processes with custom classes, simply place all your
additional Jar files into `/opt/streams/lib/`.
