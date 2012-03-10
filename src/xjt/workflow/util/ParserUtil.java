
package xjt.workflow.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.xml.sax.SAXException;

/**
 * 解析流程定义的工具类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 */
public class ParserUtil {
	/**
	 * 无参数构造函数
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * */
	public ParserUtil() {

	}
	/**
	 * 解析xml
	 * @param inputStream  流程定义文件的输入流
	 * @return document
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * @throws IOException 
	 * */
	public static Document parserXml(InputStream inputStream){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			org.w3c.dom.Document domDocument = builder.parse(inputStream);
			DOMReader reader = new DOMReader();
			doc = reader.read(domDocument);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}finally{
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
		return doc;
	}
	/**
	 * 获取文档的根元素
	 * @param document  
	 * @return 根元素RootElement
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * */
	public static Element getRootElement(Document document) {
		return document.getRootElement();
	}

	/**
	 * 获取元素的属性值
	 * @param element 元素
	 * @param attribute 属性名
	 * @return 属性值
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 */
	public static String getAttributeValue(Element element, String attribute) {
		return element.attributeValue(attribute);
	}
	/**
	 * @param root 父元素
	 * @param elementStr 子元素名
	 * @return 子元素
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 */
	public static Element getElement(Element root, String elementStr) {
		return root.element(elementStr);
		
	}
	/**
	 * @param root 父元素
	 * @param elementStr 子元素名
	 * @return 子元素
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 */
	public static Iterator getElementListByName(Element root, String elementStr) {
		return root.elementIterator(elementStr);
		
	}
	/**
	 * @param root 父元素
	 * @return 所有的子元素列表
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 */
	public static Iterator getElementList(Element root) {
		return root.elementIterator();
	}
	/**
	 * 解析xml
	 * @param xmlStr  xml格式的字符串
	 * @return document
	 * @since Oct 20, 2010
	 * @author xjt
	 * @version 1.00  Oct 20, 2010
	 * @throws IOException 
	 * */
	public static Document parserXml(String xmlStr){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			org.w3c.dom.Document domDocument = builder.parse(xmlStr);
			DOMReader reader = new DOMReader();
			doc = reader.read(domDocument);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return doc;
	}
}
