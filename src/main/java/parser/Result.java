package parser;

import java.util.List;

public class Result {
    private Integer productsCount;
    private Integer httpCount;
    private List<Product> products;

    Result(Integer productsCount, Integer httpCount, List<Product> products) {
        this.productsCount = productsCount;
        this.httpCount = httpCount;
        this.products = products;
    }

    public Integer getProductsCount() {
        return productsCount;
    }

    public Integer getHttpCount() {
        return httpCount;
    }

    public List<Product> getProducts() {
        return products;
    }
}
