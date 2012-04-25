package news.retriever.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.XMLSerializer;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class XpathUtil {

	DocumentBuilder documentBuilder;
	Tidy jtidy;

	public XpathUtil() {
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			this.jtidy = new Tidy();
			this.jtidy.setXHTML(true);
			this.jtidy.setOnlyErrors(true);
			this.jtidy.setShowWarnings(false);
			this.jtidy.setQuiet(true);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public Document stringXml2Dom(String s) {
		try {
			Document dom = this.documentBuilder.parse(new ByteArrayInputStream(s.getBytes()));
			return dom;
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Document stringHtml2Dom(String s) {
		Document result = this.jtidy.parseDOM(new ByteArrayInputStream(s.getBytes()), null);
		return result;
	}

	public void pprint(Document dom) {
		this.jtidy.pprint(dom, System.out);
	}

	@SuppressWarnings("unchecked")
	public List<Node> xpathAsNodeList(Document doc, String string) {
		try {
			DOMXPath xpath = new DOMXPath(string);
			SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
			nsContext.addNamespace("", "http://www.w3.org/1999/xhtml");
			nsContext.addNamespace("xlink", "http://www.w3.org/1999/xlink");
			xpath.setNamespaceContext(nsContext);

			List selectNodes = xpath.selectNodes(doc);
			return selectNodes;
		} catch (JaxenException e) {
			throw new RuntimeException(e);
		}
	}

	public String dom2string(Document dom) {
		StringWriter string = new StringWriter();
		XMLSerializer ser = new XMLSerializer();
		ser.setOutputCharStream(string);
		try {
			ser.serialize(dom);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return string.getBuffer().toString();
	}
}
