package similarity.model

import java.io.File
import io.{Codec, Source}

class TextBlock(val text : String) {

  def this(f: File, enc: String) {
    this(Source.fromFile(f, enc).getLines.mkString)
  }

  def this(f : File) = {
    this(f, Codec.UTF8.name)
  }

  def length = text.length

  def +(that:TextBlock) : TextBlock =  {
    val sb = new StringBuilder
    sb.append(this.text)
    sb.append(that.text)
    new TextBlock(sb.toString)
  }

  override def toString = text
}

object TextBlock {
  def apply(s : String) = {
    new TextBlock(s)
  }
}