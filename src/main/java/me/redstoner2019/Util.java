package me.redstoner2019;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

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

    public static String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        var document = parser.parse(markdown);
        return "<html>" + renderer.render(document) + "</html>";
    }

    public static String convertMillisToHMS(long millis) {
        long seconds = millis / 1000; // Convert milliseconds to seconds
        long hours = seconds / 3600;   // Calculate hours
        seconds %= 3600;                // Remaining seconds after extracting hours
        long minutes = seconds / 60;    // Calculate minutes
        seconds %= 60;                  // Remaining seconds after extracting minutes

        // Format to hh:mm:ss
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
