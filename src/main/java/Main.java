import java.sql.Connection;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DBConnection dbConnection = new DBConnection();
        DataRetriever dataRetriever = new DataRetriever(dbConnection);
        //a
        System.out.println(dataRetriever.findDishById(1));

        //c
        List<Ingredient>  ingredients = dataRetriever.findIngredients(2,2);
        for (Ingredient ingredient : ingredients) {
            System.out.println(ingredient);
        }

        /*Dish dish = new Dish(1, "Salade fraîche", DishTypeEnum.START);

        List<Ingredient> ingredientsToCreate = List.of(
                new Ingredient(6, "Fromage", 2.5, CategoryEnum.DAIRY, dish),   // supposons déjà en base
                new Ingredient(7, "Tomate", 1.5, CategoryEnum.VEGETABLE, dish),
                new Ingredient(8, "Olives", 1.0, CategoryEnum.VEGETABLE, dish)
        );

        List<Ingredient> ingredientCreated = dataRetriever.createIngredients(ingredientsToCreate);

        for (Ingredient ingredient : ingredientCreated) {
            System.out.println(ingredient);
        };*/

        //d
        Dish newDish = new Dish(6, "Pizza Margherita Test", DishTypeEnum.MAIN);
        Dish dishToCreate = dataRetriever.saveDish(newDish);
        System.out.println(dishToCreate);
        System.out.println("Dish created!");

        //e
        List<Dish> dishToFind = dataRetriever.findDishsByIngredientName("eur");
        for (Dish dish : dishToFind) {
            System.out.println(dish);
        }

        //f
        System.out.println("Ingredient to find: ");
        String ingName = null;
        CategoryEnum category = CategoryEnum.VEGETABLE;
        String dishName = null;
        int page = 1;
        int size = 10;

        List<Ingredient> ingredientListToFindByCriteria = dataRetriever.findIngredientsByCriteria(ingName, category, dishName, page, size);
        for (Ingredient ingredient : ingredientListToFindByCriteria) {
            System.out.println(ingredient);
        }







    }
}
