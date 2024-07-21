package me.redstoner2019;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class Util {
    public static void memoryInfoDump(){
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        System.out.println("Heap Memory Usage:");
        //System.out.println("Initial:              " + (heapUsage.getInit() / 1024 / 1024) + " MB");
        System.out.println("Used:                 " + (heapUsage.getUsed() / 1024 / 1024) + " MB");
        //System.out.println("Committed:            " + (heapUsage.getCommitted() / 1024 / 1024) + " MB");
        System.out.println("Max:                  " + (heapUsage.getMax() / 1024 / 1024) + " MB");

        //System.out.println("Non-Heap Memory Usage:");
        //System.out.println("Initial:              " + (nonHeapUsage.getInit() / 1024 / 1024) + " MB");
        //System.out.println("Used:                 " + (nonHeapUsage.getUsed() / 1024 / 1024) + " MB");
        //System.out.println("Committed:            " + (nonHeapUsage.getCommitted() / 1024 / 1024) + " MB");
        //System.out.println("Max:                  " + (nonHeapUsage.getMax() / 1024 / 1024) + " MB");
    }

    public static String convertMarkdownToHtml(String markdownText) {
        // Initialize the parser and renderer
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        // Parse the markdown text to a document node
        Node document = parser.parse(markdownText);

        // Render the document node to HTML
        return "<html>" + renderer.render(document) + "</html>";
    }
}
