package similarity.scripts

import org.apache.lucene.document.Field
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexWriter.MaxFieldLength
import org.apache.lucene.index._
import org.apache.lucene.analysis.standard._

import org.apache.lucene.store._
import org.apache.lucene.util._

import java.io.File



object IndexatorVise {

    def main(args: Array[String]) {

      val dirFile = new File("/home/petru/work/vise-index.idx")
      if (dirFile.exists) throw new RuntimeException("delete first " + dirFile)

      val lucenedir = FSDirectory.open(dirFile);
    	val writer = new IndexWriter(lucenedir, 
    			new StandardAnalyzer(Version.LUCENE_30), 
    			true, MaxFieldLength.UNLIMITED);
    	
    	val visedir = new File("/home/petru/work/vise_texte")
    	visedir.listFiles.foreach { (file) =>
    		val text = scala.io.Source.fromFile(file).mkString
    		val doc = new Document
    		doc.add(new Field("name", file.getName, Field.Store.YES, Field.Index.NO))
    		doc.add(new Field("text", text, Field.Store.YES, Field.Index.ANALYZED))
    		
    		writer.addDocument(doc)
    		println("adding document " + file)
    	}
    	writer.close()
    }

}