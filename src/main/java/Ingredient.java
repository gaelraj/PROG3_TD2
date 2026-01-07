public class Ingredient {
    private final int id;
    private final String name;
    private Double price;
    private CategoryEnum category;
    private Dish dish;
    private Double requiredQuantity;

    public Ingredient(int id, String name, Double price, CategoryEnum category, Dish dish, Double requiredQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
        this.requiredQuantity = requiredQuantity;
    }

    public Ingredient(int id, String name, Double price, CategoryEnum category, Dish dish) {
        this(id, name, price, category, dish, null);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public String getDishName() {
        return dish == null ? null : dish.getName();
    }

    public Double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(Double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public String toString() {
        return "ID: " + id +
                ", Name: " + name +
                ", Price: " + price +
                ", Category: " + category +
                ", Required Quantity: " + requiredQuantity +
                ", Dish: " + dish;
    }
}
