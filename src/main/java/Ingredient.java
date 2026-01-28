import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList;

    public Ingredient() {
        stockMovementList = new ArrayList<>();
    };

    public Ingredient(int id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockMovementList = new ArrayList<>();
    }

    public Integer getId() {
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

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    /*
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }*/

    public StockValue getStockValueAt(Instant at) {

        double total = 0.0;

        for (StockMovement movement : stockMovementList) {

            if (!movement.getCreationDatetime().isAfter(at)) {

                if (movement.getType() == MovementTypeEnum.IN) {
                    total += movement.getValue().getQuantity();
                } else if (movement.getType() == MovementTypeEnum.OUT) {
                    total -= movement.getValue().getQuantity();
                }
            }
        }

        return new StockValue(total, UnitEnum.KG);
    }

    public String toString() {
        return "ID: " + id +
                ", Name: " + name +
                ", Price: " + price +
                ", Category: " + category;
    }
}
