Integrating MOA
===============

MOA is a software package for online learning tasks. It provides a
large set of clustering and classifier implementations suited for
online learning. Its main intend is to serve as an environment for
evaluating online algorithms.

The *streams* framework provides the `stream-analysis` artifact, which
includes MOA and allows for integrating MOA classifiers directly into
standard stream processes.

The following example XML snippet shows the use of the Naive Bayes
implementation of MOA within a *streams* process. The example defines
a standard test-then-train process.

      <container>
           <stream id="stream" class="stream.io.CsvStream"
                   url="classpath:/multi-golf.csv.gz" limit="100"/>

           <process input="stream">
                <RenameKey from="play" to="@label" />
        
                <stream.learner.Prediction learner="NB" />

                <stream.learner.evaluation.PredictionError />

                <moa.classifiers.bayes.NaiveBayes id="NB"/>

                <stream.statistics.Sum keys="@error:NB" />
           </process>
      </container>


The <code>moa</code> packages
-----------------------------

The `stream-analysis` module of the *streams* library uses a simple
wrapper approach to integrate the MOA classes into the streams
framework. All implementations of MOA are mapped to their default
Java package, i.e.

         <moa.classifiers.bayes.NaiveBayes />

The options used in MOA are directly mapped to XML element attributes.
