package mesh123;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {

    private static final String START_URL = "https://ccis.ksu.edu.sa/en";
    private static final String ALLOWED_DOMAIN = "https://ccis.ksu.edu.sa";  // Only target this domain

    // Updated pattern to match different KSU email formats
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@(students\\.)?ksu\\.edu(\\.sa)?");

    private Set<String> visitedWebsites = new HashSet<>();
    private Queue<String> websiteQueue = new LinkedList<>();
    private Map<String, String> emailToWebsiteMap = new HashMap<>();

    public WebCrawler() {
        // Start with the initial URL
        websiteQueue.add(START_URL);
    }

    public void crawl() {
        while (!websiteQueue.isEmpty()) {
            String url = websiteQueue.poll();  // Get the next URL from the queue
            if (url == null || visitedWebsites.contains(url)) {
                continue;  // Skip if the URL is null or already visited
            }

            try {
                // Visit and parse the website
                Document doc = Jsoup.connect(url).get();
                visitedWebsites.add(url);  // Mark as visited

                // Extract emails and print results for the current website
                extractLinksAndEmails(doc, url);
                printWebsiteResults(url);  // Print the results for the current website

            } catch (IOException e) {
                // Silent mode: no print on failure
            }
        }
    }

    private void extractLinksAndEmails(Document doc, String baseUrl) {
        // Extract and add new URLs
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String absUrl = link.absUrl("href");
            if (isAllowedDomain(absUrl) && !visitedWebsites.contains(absUrl) && !websiteQueue.contains(absUrl)) {
                websiteQueue.add(absUrl);
            }
        }

        // Extract and add emails
        Matcher matcher = EMAIL_PATTERN.matcher(doc.text());
        while (matcher.find()) {
            String email = matcher.group();
            // Add email only if it's not already associated with another website
            emailToWebsiteMap.putIfAbsent(email, baseUrl);
        }
    }

    private boolean isAllowedDomain(String url) {
        return url.startsWith(ALLOWED_DOMAIN);  // Only allow URLs from https://ccis.ksu.edu.sa/
    }

    private void printWebsiteResults(String website) {
        // Check if there are any emails associated with the current website
        boolean hasEmails = false;
        for (Map.Entry<String, String> entry : emailToWebsiteMap.entrySet()) {
            String email = entry.getKey();
            String foundWebsite = entry.getValue();
            if (foundWebsite.equals(website)) {
                if (!hasEmails) {
                    // Print the website only once
                    System.out.println(website);
                    hasEmails = true;
                }
                // Print the associated email
                System.out.println("- " + email);
            }
        }
    }

    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler();
        crawler.crawl();
    }
}
