public class DishIngredient {
    private int id;
    private int dishId;
    private int ingredientId;
    private Double quantity_required;
    private UnitEnum unit;

    private Dish dish;
    private Ingredient ingredient;

    public DishIngredient(int id, int dishId, int ingredientId, Double quantity_required, UnitEnum unit) {
        this.id = id;
        this.dishId = dishId;
        this.ingredientId = ingredientId;
        this.quantity_required = quantity_required;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public Double getQuantity_required() {
        return quantity_required;
    }

    public void setQuantity_required(Double quantity_required) {
        this.quantity_required = quantity_required;
    }

    public UnitEnum getUnit() {
        return unit;
    }

    public void setUnit(UnitEnum unit) {
        this.unit = unit;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public String toString() {
        return "ID: " + id +
                " DishiId: "+ dishId +
                " IngredientId: " + ingredientId +
                " QuantityRequired: " + quantity_required + " " + unit;
    }
}
