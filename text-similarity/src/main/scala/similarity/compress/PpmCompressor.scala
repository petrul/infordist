package similarity.compress

import similarity.model.TextBlock
import com.colloquial.arithcode.{ArithCodeInputStream, PPMModel, ArithCodeOutputStream}
import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

class PpmCompressor  extends Compressor {
  def compress(text: TextBlock, ostream: OutputStream) = {

    val ppmos = new ArithCodeOutputStream(ostream, new PPMModel(8))
    ppmos.write(text.text.getBytes())
    ppmos.flush
  }

  def uncompress(istream: InputStream) = {
    val acis = new ArithCodeInputStream(istream, new PPMModel(8))
    val baos = new ByteArrayOutputStream

    Util.copyStream(acis, baos)
    TextBlock(new String(baos.toByteArray))
  }
}