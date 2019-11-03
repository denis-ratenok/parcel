package parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static String ABOUTYOU_API = "https://api-cloud.aboutyou.de/v1/products";

    public static void main(String[] args) throws Exception {
        URIBuilder b = new URIBuilder(Main.ABOUTYOU_API);
        b.addParameter("with", "attributes:key(brand|name|colorDetail),priceRange,siblings,siblings.attributes:key(brand|name|colorDetail),siblings.priceRange");
        b.addParameter("filters[category]", "20290");
        b.addParameter("sortDir", "desc");
        b.addParameter("sortScore", "category_scores");
        b.addParameter("sortChanel", "etkp");
        b.addParameter("page", "1");
        b.addParameter("campaignKey", "px");
        b.addParameter("shopId", "139");

        String url = b.build().toString();
        Integer httpCount = 0;
        String firstPageOneItemJson = Jsoup.connect(url)
                .ignoreContentType(true)
                .execute()
                .body();
        httpCount++;

        List<Product> products = new ArrayList<>();

        JSONObject response = new JSONObject(firstPageOneItemJson);
        // Just for test
        Main.checkCorrectnessProductsQuantity(response.getJSONObject("pagination").getInt("last"));

        JSONArray entities = response.getJSONArray("entities");
        for (Object entry : entities) {
            JSONObject entity = new JSONObject(entry.toString());
            products.add(Main.getProductByEntity(entity));
            for (Object sibling : entity.getJSONArray("siblings")) {
                if (!new JSONObject(sibling.toString()).getBoolean("isActive")) {
                    continue;
                }
                products.add(Main.getProductByEntity(new JSONObject(sibling.toString())));
            }

        }

        products = products.stream().distinct().collect(Collectors.toList());
        Result result = new Result(products.size(), httpCount, products);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File("./result.json"), result);

    }


    static private Product getProductByEntity(JSONObject entity) {
        JSONObject attributes = entity.getJSONObject("attributes");
        JSONObject colorDetail = attributes.getJSONObject("colorDetail");

        Integer id = entity.getInt("id");
        String productName = attributes.getJSONObject("name").getJSONObject("values").getString("label");
        String brandName = attributes.getJSONObject("brand").getJSONObject("values").getString("label");
        String color = "";
        JSONArray colorsValues;
        try {
            colorsValues = colorDetail.getJSONArray("values");
            for (Object j : colorsValues) {
                color = color.concat(new JSONObject(j.toString()).getString("label") + " ");
            }
        } catch (JSONException e) {
            color = colorDetail.getJSONObject("values").getString("label");
        }

        String price;
        if (entity.getBoolean("isSoldOut")) {
            price = "-1";
        } else if (entity.getJSONObject("priceRange").getJSONObject("min").getInt("withTax")
                != entity.getJSONObject("priceRange").getJSONObject("max").getInt("withTax")) {
            price = "from: " + entity.getJSONObject("priceRange").getJSONObject("min").getInt("withTax") + " "
                    + entity.getJSONObject("priceRange").getJSONObject("min").getString("currencyCode");
        } else {
            price = entity.getJSONObject("priceRange").getJSONObject("min").getInt("withTax") + " "
                    + entity.getJSONObject("priceRange").getJSONObject("min").getString("currencyCode");
        }

        return new Product(id, productName, brandName, color, price);
    }


    static private void checkCorrectnessProductsQuantity(Integer count) throws IOException, NotCorrectProductQuantityException {
        Document doc = Jsoup.connect("https://www.aboutyou.de/maenner/bekleidung")
                .get();
        Elements paginationWrapper = doc.select("div[data-test-id=\"PaginationWrapper\"]");
        List<Element> paginationLiList = paginationWrapper.select("ul li");
        Integer pagesCount = Integer.parseInt(paginationLiList.get(paginationLiList.size() - 2).text());
        if (!pagesCount.equals(count)) {
            throw new NotCorrectProductQuantityException();
        }
    }
}
