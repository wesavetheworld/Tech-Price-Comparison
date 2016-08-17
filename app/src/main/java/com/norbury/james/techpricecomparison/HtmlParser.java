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
// TODO Consider refactoring Amazon and Ebuyer parsers
class HtmlParser {
    private final Element body;
    private final String source;
    /**
     * @param htmlString Contains full page in HTML style string.
     * @param source Website HTML code game from to determine scraping methods.
     */
    public HtmlParser(String htmlString, String source) {
        body = Jsoup.parse(htmlString).body();
        this.source = source;
    }

    /**
     * Returns list of products to be used by Adapter.
     * @return Results from Amazon or Ebuyer. If neither source can be determined null is returned.
     */
    public List<Product> getProducts() {
        switch (source) {
            case "www.amazon.co.uk":
                return getAmazonProducts();
            case "www.ebuyer.com":
                return getEbuyerProducts();
            default:
                return null;
        }
    }

    /**
     * Facade for getting title, price, and picture.
     * @return List of producs.
     */
    private List<Product> getAmazonProducts() {
        // li containing search results including pictures, prices, titles
        Elements results = body.getElementsByClass("s-result-item");
        List<Product> products = new ArrayList<>();
        // Each item is <li> tag
        for (Element result : results) {
            String title = getAmazonTitle(result);
            float price = getAmazonPrice(result);
            String image = getAmazonImage(result);
            products.add(new Product(title, price, image));
        }
        return products;
    }

    /**
     *
     * @param container jsoup Element.
     * @return Name of product in search result.
     */
    private String getAmazonTitle(Element container) {
        String productTitle = "";
        // Get all <h2> as contains product title
        // TODO Use first() when certain only one element returned or only use first
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

    /**
     * @param container jsoup Element.
     * @return Price of product in search result.
     */
    private float getAmazonPrice(Element container) {
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

    /**
     * @param container jsoup Element.
     * @return URL to image from <src> tag.
     */
    private String getAmazonImage(Element container) {
        Elements images = container.getElementsByTag("img");
        String src = "";
        for (Element image : images) {
            src = image.attr("src");
        }
        return src;
    }

    /**
     * Facade for getting title, price, and picture.
     * @return List of producs.
     */
    private List<Product> getEbuyerProducts() {
        // All search results contained in <div>
        Elements results = body.getElementsByClass("searchResult");
        List<Product> products = new ArrayList<>();
        for (Element result : results) {
            String title = getEbuyerTitle(result);
            float price = getEbuyerPrice(result);
            String image = getEbuyerImage(result);
            products.add(new Product(title, price, image));
        }
        return products;
    }

    /**
     * @param container jsoup Element.
     * @return Name of product in search result.
     */
    private String getEbuyerTitle(Element container) {
        Element title = container.getElementsByTag("h1").first();
        return title.text();
    }

    /**
     * @param container jsoup Element.
     * @return Price of product in search result.
     */
    private float getEbuyerPrice(Element container) {
        Element price = container.getElementsByTag("h2").first();
        String productPriceString = price.text().trim().split(" ")[0];
        productPriceString = productPriceString.substring(1);
        float productPrice;
        try {
            productPrice = Float.parseFloat(productPriceString);
        } catch (NumberFormatException e) {
            productPrice = -1;
            e.printStackTrace();
        }
        return productPrice;
    }

    /**
     * @param container jsoup Element.
     * @return URL to image from <src> tag.
     */
    private String getEbuyerImage(Element container) {
        Element image = container.getElementsByTag("img").first();
        return image.attr("src");
    }
}
