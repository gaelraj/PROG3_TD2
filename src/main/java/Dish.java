import java.util.List;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;

    public Dish(int id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
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

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        for (int i = 0; i < ingredients.size(); i++) {
            ingredients.get(i).setDish(this);
        }
        this.ingredients = ingredients;
    }

    public double getDishCost() {
        if (this.ingredients == null || this.ingredients.isEmpty()) {
            return 0.0;
        }

        for (Ingredient ingredient : this.ingredients) {
            if (ingredient.getRequiredQuantity() == null) {
                throw new IllegalStateException(
                        "Cannot calculate dish cost: required quantity is unknown for ingredient '"
                                + ingredient.getName() + "'"
                );
            }
        }

        return this.ingredients.stream()
                .mapToDouble(ingredient -> ingredient.getPrice() * ingredient.getRequiredQuantity())
                .sum();
    }

    public String toString() {
        return "ID: " + id + " Name: " + name + " DishType: " + dishType;
    }

}
