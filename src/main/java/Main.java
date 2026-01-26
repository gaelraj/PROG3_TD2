import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        DBConnection dbConnection = new DBConnection();
        DataRetriever dataRetriever = new DataRetriever(dbConnection);

        //Test for getDishCost method :
        // Nom du plat        : id
        // Salade fraîche     : 1
        // Poulet grillé      : 2
        // Riz aux légumes    : 3
        // Gâteau au chocolat : 4
        // Salade de fruits   : 5
        Double dishcost = dataRetriever.findDishById(5).getDishCost();
        System.out.println(dishcost);

        //Test for getGrossMargin method:
        // Nom de l'ingredient : id
        // Laitue              : 1
        // Tomate              : 2
        // Poulet              : 3
        // Chocolat            : 4
        // Beurre              : 5

        Double dishGrossMargin = dataRetriever.findDishById(5).getGrossMargin();
        System.out.println(dishGrossMargin);



    }
}
