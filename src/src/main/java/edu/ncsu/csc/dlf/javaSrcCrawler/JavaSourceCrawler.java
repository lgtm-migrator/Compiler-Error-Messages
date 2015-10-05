package edu.ncsu.csc.dlf.javaSrcCrawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import edu.ncsu.csc.dlf.JComp;

public class JavaSourceCrawler {
	
	private static final String UTF_8 = "utf-8";
	private static final String OP_TXT = "data" + File.separator + "op.txt";
	private static final String JVA_SRC = "data"+ File.separator + "jvaSrc" +  File.separator;
	private static final String JAVA_ERR_EXAMPLES_URL = "http://cr.openjdk.java.net/~jjg/diags-examples.html";

	public static void main(String[] args) {
		
		JavaSourceCrawler crawler = new JavaSourceCrawler();
		Document doc = crawler.getDoc(JAVA_ERR_EXAMPLES_URL);
		Map<String, String> srcMap =  crawler.getClassMap(doc);
		List<String> errStr = crawler.getError(srcMap);
		crawler.writeOp(errStr);
		crawler.writeMap(srcMap);
		crawler.cleanup();
	}
	
	private void cleanup() {
		File dir = new File(".");
		File [] files = dir.listFiles(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return name.endsWith(".class");
			}
		});

		for (File classFile : files) {
		    classFile.delete();
		}
		
	}

	private void writeOp(List<String> errStr) {
		
			Writer writer = null;

			try {
			    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OP_TXT), UTF_8));
			    for(String line: errStr)
				{
			    	writer.write(line);
			    	writer.write("\n");
				}
			} catch (IOException ex) {
			  // report
			} finally {
			   try {writer.close();} catch (Exception ex) {}
			}
	}

	private List<String> getError(Map<String, String> srcMap) {
		JComp jc = new JComp();
		List<String> returnList = new ArrayList<String>();
		for(String fileName: srcMap.keySet())
		{
			returnList.add(jc.compile(fileName.replace(".java", ""), srcMap.get(fileName)));
		}
		return returnList;
	}

	private void writeMap(Map<String, String> srcMap) {
		for(String fileName: srcMap.keySet())
		{
			Writer writer = null;

			try {
			    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(JVA_SRC + fileName), UTF_8));
			    writer.write(srcMap.get(fileName));
			} catch (IOException ex) {
			  // report
			} finally {
			   try {writer.close();} catch (Exception ex) {}
			}
		}
		
	}

	private Map<String, String> getClassMap(Document doc) {
		Map<String, String> returnMap = new HashMap<String, String>();
		Elements eleList = doc.select("h4");
		for(Element element:eleList)
		{
			if(element.text().toString().endsWith(".java"))
			{
				if(element.nextElementSibling().nodeName().equalsIgnoreCase("div")&&element.nextElementSibling().className().equals("file"))
				{
					String javaText = getText(element.nextElementSibling().child(1));
					
					System.err.println(element.text() +"\n" + javaText);
					if(returnMap.containsKey(element.text()))
						System.out.println("Here");
					returnMap.put(element.text(), javaText);
				}
			}
		}
		return returnMap;
	}
	
	private String getText(Element parentElement) {
	     String working = "";
	     for (Node child : parentElement.childNodes()) {
	          if (child instanceof TextNode) {
	              working += ((TextNode)child).getWholeText();
	          }
	          if (child instanceof Element) {
	              Element childElement = (Element)child;
	              // do more of these for p or other tags you want a new line for
	              if (childElement.tag().getName().equalsIgnoreCase("br")) {
	                   working += "\n";
	              }                  
	              working += getText(childElement);
	          }
	     }

	     return working;
	 }

	private Document getDoc(String url)
	{
		Document doc = null; 
		try{
			doc = Jsoup.parse(new URL(url), 10000000);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.err);
		}
		return doc;
	}
}
