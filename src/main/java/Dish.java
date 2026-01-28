import java.util.ArrayList;
import java.util.List;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private Double price;
    private List<DishIngredient> dishIngredients;

    public Dish() {
        this.dishIngredients = new ArrayList<>();
    }

    public Dish(int id, String name, DishTypeEnum dishType,Double price) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        this.dishIngredients = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    public double getDishCost() {
        if (this.dishIngredients == null || this.dishIngredients.isEmpty()) {
            return 0.0;
        }

        double totalCost = 0.0;

        for (DishIngredient dishIngredient : this.dishIngredients) {
            if (dishIngredient.getIngredient() != null &&
                dishIngredient.getIngredient().getPrice() != null &&
                dishIngredient.getQuantity_required() != null
            ) {
                double ingredientCost = dishIngredient.getIngredient().getPrice() *
                                        dishIngredient.getQuantity_required();
                totalCost += ingredientCost;
            }
        }

        return totalCost;

    }

    public double getGrossMargin() {
        if (this.price == null ) {
            throw new RuntimeException("No price has been specified: "+ price);
        }

        double price = this.price - this.getDishCost();

        return price;
    }

    public String toString() {
        return "ID: " + id + " Name: " + name + " DishType: " + dishType + " Price: " + price;
    }

}
