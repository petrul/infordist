package similarity.cli

import store.termmatrix.TermMatrixReadOnly
import java.io.File
import swing._

object TermMatrixInspector extends SimpleSwingApplication {
  var dir : String = null
  var store: TermMatrixReadOnly = null

  def top = new MainFrame {
    title = "Term Matrix Inspector"
    val label = new Label { text = dir }
    val list = new swing.ListView[String](store.getTerms)
    println(store.getTerms.toList)
    val button= new Button {
      text = "click!"
    }
    contents = new BoxPanel(Orientation.Vertical) {
      contents += label
      contents += list
      contents += button
    }
  }


  override def startup(args: Array[String]) {
    dir = args(0)
    store = new TermMatrixReadOnly(new File(dir))
    println(store.getTerms)
    super.startup(args);
  }

}