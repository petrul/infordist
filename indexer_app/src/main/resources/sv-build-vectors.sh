# calls semantic vectors code that actually build the freaking vectors from a Lucene index

M2_REPO=~/.m2/repository

CP=
CP=$CP:$M2_REPO/pitt/search/semanticvectors/1.22/semanticvectors-1.22.jar

CP=$CP:$M2_REPO/org/apache/lucene/lucene-core/2.4.1/lucene-core-2.4.1.jar
CP=$CP:$M2_REPO/org/apache/lucene/lucene-demos/2.4.1/lucene-demos-2.4.1.jar
#echo "classpath : $CP"

java -Xmx3000m -cp $CP pitt.search.semanticvectors.BuildIndex $*

