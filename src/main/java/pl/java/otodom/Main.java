package pl.java.otodom;

import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Instant start = Instant.now();

        URL otodom = new URL("https://www.otodom.pl/sprzedaz/mieszkanie/szczecin/");
        Set<String> urls = getURLs(otodom);


        var executor = Executors.newFixedThreadPool(8);

        AtomicInteger atomicInteger = new AtomicInteger(0);
        urls.forEach(url -> executor.execute(() -> {
            try {
                readPage(new URL(url), String.valueOf(atomicInteger.incrementAndGet()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Instant stop = Instant.now();
        System.out.println(Duration.between(start, stop).toMillis());
    }

    private static void readPage(URL url, String fileName) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(url.openStream()))) {

            String inputLine;
            StringBuilder builder = new StringBuilder();
            while((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
                builder.append(System.lineSeparator());
            }
            String content = builder.toString();
            savePageContentToFile(content, fileName);
        }
    }

    private static void savePageContentToFile(String content, String fileName) {
        try (var out = new BufferedWriter(new FileWriter(fileName + ".html", false))) {
            out.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> getURLs(URL fromPage) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(fromPage.openStream()));

        String inputLine;
        StringBuilder builder = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            builder.append(inputLine);
            builder.append(System.lineSeparator());
        }
        in.close();

        String content = builder.toString();
        Set<String> urls = new HashSet<>();

        for (int i = 0; i < content.length(); i++) {
            i = content.indexOf("https://www.otodom.pl/pl/oferta", i);
            if (i == -1) {
                break;
            }
            String url = content.substring(i).split(".html")[0];
            urls.add(url);
        }

        return urls;
    }
}
