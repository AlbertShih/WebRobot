package itri;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

//import java.net.*;
import java.io.*;
//import java.net.URL;
//import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

public class WebRobot {
	public static void main(String[] args) throws Exception {
		//測試		
		JFrame f = new JFrame("Grabbing Content");
		f.setSize(1000, 600);
	    f.setLocation(200, 200);
		f.setLayout(new BorderLayout());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel jl = new JLabel("URL: ");
		final JTextField jtf = new JTextField(45);
		JPanel jp1 = new JPanel();
		jp1.add(jl);
		jp1.add(jtf);
		f.add(jp1, BorderLayout.NORTH);

		final JTextArea jta = new JTextArea();
		JScrollPane jsp = new JScrollPane(jta);
		f.add(jsp, BorderLayout.CENTER);
		JButton jb = new JButton("Get");
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					jta.setText("");
					String url = jtf.getText();
					//Document doc = Jsoup.parse(url, 3000);
					Document doc = Jsoup.connect(url)
				      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1944.0 Safari/537.36")
				      .referrer("http://www.google.com")
				      .timeout(8000)
				      .get();
				      
					//String text = doc.select("td.content").text();
					//jta.append("文章全文(text)：\n" + text + "\n\n");
					
			        WebRobot formatter = new WebRobot();
			        String plainText = doc.select("div.subject").text();//標題
			        jta.append("標題：\n" + plainText + "\n\n\n");
			        plainText = doc.select("div#breadcrumb").select("a[href*=channel]").text();//分類
			        jta.append("分類：\n" + plainText + "\n\n\n");
			        plainText = doc.select("div.author").text();//作者與上傳時間
			        jta.append("作者與上傳時間：\n" + plainText + "\n\n\n");
			        plainText = doc.select("div.tags").text();//標籤(Tags)
			        jta.append("標籤(Tags)：\n" + plainText + "\n\n\n");
			        plainText = formatter.getPlainText(doc.select("div.postcontent").first());//文章全文(in-line formatting) 
			        jta.append("文章全文(in-line formatting)：\n" + plainText + "\n\n\n");
			        
				}catch (IOException ioe) {}
			}
		});
		  f.add(jb, BorderLayout.SOUTH);
		  f.setVisible(true);
	
        
    
        
	}
	
	public String getPlainText(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element); // walk the DOM, and call .head() and .tail() for each node

        return formatter.toString();
    }
	  // the formatting rules, implemented in a breadth-first DOM traverse
    	
	private class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if (name.equals("li"))
                append("\n * ");
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("br"))
                append("\n");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n\n");
            else if (name.equals("a"))
                append(String.format(" <%s>", node.absUrl("href")));
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
            if (text.equals(" ") &&
                    (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                        word = word + " ";
                    if (word.length() + width > maxWidth) { // wrap and reset counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        public String toString() {
            return accum.toString();
        }
    }
}
