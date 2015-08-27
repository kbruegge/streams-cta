Quickstart
==========

Designing a simple stream process does not require more than writing
some XML declaration and executing that XML with the stream-runner as
shown in the following figure:

<div class="figure">
   <img src="quickstart-xml.png" style="height: 180px;" />
</div>

The simple example presented below, defines a single process that
reads from a CSV stream and prints out the data items to standard
output:


    <container>
        <stream id="firstStream" class="stream.io.CsvStream"
                url="http://www.jwall.org/streams/sample-stream.csv" />

        <process input="firstStream">
            <PrintData />
        </process>
     </container>

The *stream-runner* required to execute this stream is a simple executable
Java archive available for download:  [stream-runner.jar](stream-runner.jar).


Running the Stream Process
--------------------------

The simple process defined above can be run by

     # java -jar stream-runner.jar first-process.xml

The process will simply read the stream in CSV-format and execute the
processor `PrintData` for each item obtained from the stream.
