package similarity.scripts

import scala.xml._
import java.io.{Console => JConsole,_}
import java.util.zip._


/**
 * read an ODT file of dreams and writes each dream to an individual
 * @author petru
 */
object CitesteToateVisele {
	
    def main(args: Array[String]) {
    	
    	val viseDoc = new java.io.File("/home/petru/Dropbox/perso/deimprimat/toate-visele.odt")
    	val zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(viseDoc)))
    	
    	var entry:ZipEntry = null;
    	do { entry = zis.getNextEntry() } while (entry != null && entry.getName() != "content.xml")
    	
    	val content = XML.load(zis)
    	val paragrafe = (content \\ "p").toList
   	
    	val attr_styleName = "@{urn:oasis:names:tc:opendocument:xmlns:text:1.0}style-name"
    	var vise = paragrafe.filter(x => (x \ attr_styleName toString()) == "Vis").map(_.text)
    	vise = vise.filter(_.trim != "") // remove blank lines
    	
    	val outdir = new File("/home/petru/work/vise_texte")
    	if (outdir.exists) throw new RuntimeException("will not continue, directory " + outdir + " already exists")
    	outdir.mkdirs()
    	
    	// write each dream to a separate file
    	for (i:Int <- 1 to vise.length) {
    		val crtDream = vise(i - 1)
    		def filename(i:Int, dream: String) = {
	    		val defaultExcerptSize = 30
	    		val excerptSize = if (crtDream.length > defaultExcerptSize) defaultExcerptSize else crtDream.length - 1
	    		val zeroPaddedCounter = String.format("%04d", i.asInstanceOf[Integer])
	    		zeroPaddedCounter + "." + crtDream.substring(0, excerptSize).replace(" ", "_") + ".txt"
    		}
    		val writer = new FileWriter(new File(outdir, filename(i, crtDream)))
    		try {
    			writer.write(crtDream)
    		} finally {writer.close()}
    	}
    		
    	println("vise:" + vise.length)
    }
    
}