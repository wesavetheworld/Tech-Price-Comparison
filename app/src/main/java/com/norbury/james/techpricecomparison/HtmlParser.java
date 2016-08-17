package com.norbury.james.techpricecomparison;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 11/08/2016.
 * Uses JSOUP to parse HTML and return products from search URLs.
 */
public class HtmlParser {
    private Element body;
    // TODO javadoc comments throughout class
    /**
     * @param htmlString Contains full page in HTML style string
     */
    public HtmlParser(String htmlString) {
        body = Jsoup.parse(htmlString).body();
    }

    public List<Product> getProducts() {
        // li containing search results including pictures, prices, titles
        Elements results = body.getElementsByClass("s-result-item");
        List<Product> products = new ArrayList<>();
        // Each item is <li> tag
        for (Element result : results) {
            String title = getTitle(result);
            float price = getPrice(result);
            String image = getImage(result);
            products.add(new Product(title, price, image));
        }
        return products;
    }

    private String getTitle(Element container) {
        String productTitle = "";
        // Get all <h2> as contains product title
        Elements titles = container.getElementsByTag("h2");
        // Search all <h2>
        for (Element title : titles) {
            // Does it have "data-attribute" attribute
            if (title.hasAttr("data-attribute")) {
                // Get title as string
                productTitle = title.attr("data-attribute");
            }
        }
        return productTitle;
    }

    private float getPrice(Element container) {
        float productPrice = -1;
        Elements prices = container.getElementsByTag("span");
        // Search all <span>
        for (Element price : prices) {
            // Does it have "s-price" class
            if (price.hasClass("s-price")) {
                // Get price as String
                String priceString = price.text();
                // Remove £ sign
                priceString = priceString.substring(1);
                try {
                    productPrice = Float.parseFloat(priceString);
                } catch (NumberFormatException e) {
                    productPrice = -1;
                    e.printStackTrace();
                }
            }
        }
        return productPrice;
    }

    private String getImage(Element container) {
        Elements images = container.getElementsByTag("img");
        String src = "";
        for (Element image : images) {
            src = image.attr("src");
        }
        return src;
    }
}
