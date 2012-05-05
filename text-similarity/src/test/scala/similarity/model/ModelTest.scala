package similarity.model

import org.scalatest.{Tag, FunSuite}


class ModelTest extends FunSuite {
  test("TextBlock", Tag("model")) {

    var txt : TextBlock = null

    val someValue = "from the bottom to the top to the end"

    txt = new TextBlock(someValue)
    assert(txt.text == someValue)

    txt = TextBlock(someValue)
    assert(txt.text == someValue)

  }
}