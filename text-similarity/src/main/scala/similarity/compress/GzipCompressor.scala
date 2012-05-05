package similarity.compress

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.util.zip.GZIPOutputStream
import similarity.model.TextBlock

class GzipCompressor  extends Compressor {

  def compress(text: TextBlock, ostream: OutputStream): Unit = {
    val gzos = new GZIPOutputStream(ostream)
    gzos.write(text.text.getBytes())
    gzos.flush
    gzos.close
  }

  def uncompress(istream: InputStream) = {
    error("unimplemented")
  }
}