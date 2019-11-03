package parser;

class Product {
    private Integer id;
    private String name;
    private String brand;
    private String color;
    private String price;

    Product(Integer id, String name, String brand, String color, String price) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.color = color;
        this.price = price;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        return ((Product) obj).id.equals(this.id);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getColor() {
        return color;
    }

    public String getPrice() {
        return price;
    }
}
