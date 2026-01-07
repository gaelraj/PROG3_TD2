import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection dbConnection;

    public DataRetriever(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Dish findDishById(Integer id) {
        String dishQuery = """
            SELECT Dish.id as dish_id, Dish.name as dish_name, dish_type
            FROM Dish
            WHERE id = ?  
            """;

        String ingredientsQuery = """
            SELECT i.id AS ingredient_id, 
                   i.name AS ingredient_name, 
                   i.price, 
                   i.category, 
                   i.required_quantity
            FROM ingredient i
            WHERE i.id_dish = ?
            ORDER BY i.id
            """;

        try (Connection connection = dbConnection.getDBConnection()) {

            Dish dish = null;
            try (PreparedStatement preparedStatement = connection.prepareStatement(dishQuery)) {
                preparedStatement.setInt(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        dish = new Dish(
                                resultSet.getInt("dish_id"),
                                resultSet.getString("dish_name"),
                                DishTypeEnum.valueOf(resultSet.getString("dish_type"))
                        );
                    } else {
                        throw new RuntimeException("Dish not found");
                    }
                }
            }

            List<Ingredient> ingredients = new ArrayList<>();

            try (PreparedStatement ingredientsStmt = connection.prepareStatement(ingredientsQuery)) {
                ingredientsStmt.setInt(1, id);

                try (ResultSet ingredientsRs = ingredientsStmt.executeQuery()) {
                    while (ingredientsRs.next()) {
                        Double requiredQuantity = null;
                        Object qtyObj = ingredientsRs.getObject("required_quantity");
                        if (qtyObj != null) {
                            requiredQuantity = ingredientsRs.getDouble("required_quantity");
                        }

                        Ingredient ingredient = new Ingredient(
                                ingredientsRs.getInt("ingredient_id"),
                                ingredientsRs.getString("ingredient_name"),
                                ingredientsRs.getDouble("price"),
                                CategoryEnum.valueOf(ingredientsRs.getString("category")),
                                dish,
                                requiredQuantity
                        );

                        ingredients.add(ingredient);
                    }
                }
            }

            dish.setIngredients(ingredients);

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = """
                SELECT Ingredient.id as ingredient_id, 
                   Ingredient.name as ingredient_name, 
                   Ingredient.price as ingredient_price, 
                   Ingredient.category,
                   Ingredient.required_quantity,
                   Dish.id as dish_id, 
                   Dish.name as dish_name, 
                   dish_type 
            FROM Ingredient
            INNER JOIN Dish ON Ingredient.id_dish = Dish.id
            ORDER BY Ingredient.id
            LIMIT ? OFFSET ?
                """;
        int offset = (page - 1) * size;

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setInt(1, size);
            preparedStatement.setInt(2, offset);

            try (ResultSet resultSet = preparedStatement.executeQuery();) {
                while (resultSet.next()) {
                    Dish dish = new Dish(
                            resultSet.getInt("dish_id"),
                            resultSet.getString("dish_name"),
                            DishTypeEnum.valueOf(resultSet.getString("dish_type"))
                    );

                    Double requiredQuantity = null;
                    Object qtyObj = resultSet.getObject("required_quantity");

                    if (qtyObj != null) {
                        requiredQuantity = resultSet.getDouble("required_quantity");
                    }

                    Ingredient ingredient = new Ingredient(
                            resultSet.getInt("ingredient_id"),
                            resultSet.getString("ingredient_name"),
                            resultSet.getDouble("ingredient_price"),
                            CategoryEnum.valueOf(resultSet.getString("category")),
                            dish,
                            requiredQuantity
                    );

                    ingredients.add(ingredient);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        };
        return ingredients;
    };

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        List<Ingredient> savedIngredients = new ArrayList<>();

        String checkingQuery = "SELECT COUNT(*) FROM ingredient WHERE name = ?";
        String insertionQuery = "INSERT INTO ingredient (id, name, price, category, id_dish, required_quantity) VALUES (?, ?, ?, ?::category, ?, ?)";

        try (Connection connection = dbConnection.getDBConnection()
        ) {
            connection.setAutoCommit(false);

            try (PreparedStatement checkStmt = connection.prepareStatement(checkingQuery);
                 PreparedStatement insertStmt = connection.prepareStatement(insertionQuery)
            ) {
                for (Ingredient ingredient : newIngredients) {
                    checkStmt.setString(1, ingredient.getName());
                    try (ResultSet resultSet = checkStmt.executeQuery()
                    ) {
                        resultSet.next();
                        int count = resultSet.getInt(1);
                        if (count > 0) {
                            throw new RuntimeException("Ingredient already exists: "+ ingredient.getName());
                        }
                    }

                    insertStmt.setInt(1, ingredient.getId());
                    insertStmt.setString(2, ingredient.getName());
                    insertStmt.setDouble(3,ingredient.getPrice());
                    insertStmt.setString(4, ingredient.getCategory().name());

                    if (ingredient.getDish() != null) {
                        insertStmt.setInt(5, ingredient.getDish().getId());
                    } else {
                        insertStmt.setNull(5, java.sql.Types.INTEGER);
                    }

                    if (ingredient.getRequiredQuantity() != null) {
                        insertStmt.setDouble(6, ingredient.getRequiredQuantity());
                    } else {
                        insertStmt.setNull(6, java.sql.Types.NUMERIC);
                    }

                    insertStmt.executeUpdate();
                    savedIngredients.add(ingredient);
                }
                connection.commit();
                return savedIngredients;
            } catch ( Exception e ) {
                connection.rollback();
                throw new RuntimeException("Error creating ingredients: " + e.getMessage());
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating ingredients: " + e.getMessage());
        }
    }

    public Dish saveDish(Dish dishToSave) {
        String checkingDishQuery = "SELECT COUNT(*) FROM dish WHERE id = ?";
        String insertDishSql = "INSERT INTO dish (id, name, dish_type) VALUES (?, ?, ?::dish_type)";
        String updateDishSql = "UPDATE dish SET name = ?, dish_type = ?::dish_type WHERE id = ?";
        String deleteIngredientsSql = "DELETE FROM ingredient WHERE id_dish = ?";
        String insertIngredientSql = "INSERT INTO ingredient (id, name, price, category, id_dish, required_quantity) VALUES (?, ?, ?, ?::category, ?, ?)";

        try (Connection connection = dbConnection.getDBConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean exists;
                try (PreparedStatement checkStmt = connection.prepareStatement(checkingDishQuery)) {
                    checkStmt.setInt(1, dishToSave.getId());
                    try (ResultSet resultSet = checkStmt.executeQuery()) {
                        resultSet.next();
                        exists = resultSet.getInt(1) > 0;
                    }
                }
                if (!exists) {
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertDishSql)) {
                        insertStmt.setInt(1, dishToSave.getId());
                        insertStmt.setString(2, dishToSave.getName());
                        insertStmt.setString(3, dishToSave.getDishType().name());
                        insertStmt.executeUpdate();
                    }
                    System.out.println("Dish created with ID: " + dishToSave.getId());
                } else {
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateDishSql)) {
                        updateStmt.setString(1, dishToSave.getName());
                        updateStmt.setString(2, dishToSave.getDishType().name());
                        updateStmt.setInt(3, dishToSave.getId());
                        updateStmt.executeUpdate();
                    }
                    System.out.println("Dish updated with ID: " + dishToSave.getId());
                }

                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteIngredientsSql)) {
                    deleteStmt.setInt(1, dishToSave.getId());
                    deleteStmt.executeUpdate();
                }

                if (dishToSave.getIngredients() != null) {
                    try (PreparedStatement insertIngStmt = connection.prepareStatement(insertIngredientSql)) {
                        for (Ingredient ing : dishToSave.getIngredients()) {

                            insertIngStmt.setInt(1, ing.getId());
                            insertIngStmt.setString(2, ing.getName());
                            insertIngStmt.setDouble(3, ing.getPrice());
                            insertIngStmt.setString(4, ing.getCategory().name());
                            insertIngStmt.setInt(5, dishToSave.getId());

                            if (ing.getRequiredQuantity() != null) {
                                insertIngStmt.setDouble(6, ing.getRequiredQuantity());
                            } else {
                                insertIngStmt.setNull(6, java.sql.Types.NUMERIC);
                            }

                            insertIngStmt.executeUpdate();
                        }
                    }
                }

                connection.commit();
                return findDishById(dishToSave.getId());

            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Error saving dish: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Connection Error: " + e.getMessage(), e);
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        String query = "SELECT d.id AS dish_id, d.name AS dish_name, d.dish_type, " +
                "i.id AS ingredient_id, i.name AS ingredient_name, i.price, i.category " +
                "FROM dish d " +
                "JOIN ingredient i ON d.id = i.id_dish " +
                "WHERE i.name ILIKE ? " +
                "ORDER BY d.id";

        List<Dish> dishList = new ArrayList<>();

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, "%" + ingredientName + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()
            ) {
                while (resultSet.next()) {
                    int dish_id = resultSet.getInt("dish_id");
                    
                    Dish dishCourant = null;
                    for (Dish dish : dishList) {
                        if (dish.getId() == dish_id) {
                            dishCourant = dish;
                            break;
                        }
                    }
                    
                    if (dishCourant == null) {
                        dishCourant = new Dish(
                                dish_id,
                                resultSet.getString("dish_name"),
                                DishTypeEnum.valueOf(resultSet.getString("dish_type"))
                        );
                        dishCourant.setIngredients(new ArrayList<>());
                        dishList.add(dishCourant);
                    }
                    
                    Ingredient ingredient = new Ingredient(
                            resultSet.getInt("ingredient_id"),
                            resultSet.getString("ingredient_name"),
                            resultSet.getDouble("price"),
                            CategoryEnum.valueOf(resultSet.getString("category")),
                            dishCourant
                    );
                    dishCourant.getIngredients().add(ingredient);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding dish: " + e.getMessage());
        }

        return dishList;
    };

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {

        if (page < 1 || size <= 0) {
            throw new IllegalArgumentException("Page must be >= 1 and size must be > 0");
        }

        List<Ingredient> ingredientList = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT i.id AS ingredient_id, i.name AS ingredient_name, i.price, i.category, " +
                        "i.required_quantity, " +  // Ajout ici
                        "d.id AS dish_id, d.name AS dish_name, d.dish_type " +
                        "FROM ingredient i " +
                        "JOIN dish d ON i.id_dish = d.id WHERE 1=1"
        );

        if (ingredientName != null && !ingredientName.isEmpty()) {
            sqlBuilder.append(" AND i.name ILIKE ?");
        }
        if (category != null) {
            sqlBuilder.append(" AND i.category = ?::category");
        }
        if (dishName != null && !dishName.isEmpty()) {
            sqlBuilder.append(" AND d.name ILIKE ?");
        }

        sqlBuilder.append(" ORDER BY i.id LIMIT ? OFFSET ?");

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;

            if (ingredientName != null && !ingredientName.isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + ingredientName + "%");
            }
            if (category != null) {
                preparedStatement.setString(paramIndex++, category.name());
            }
            if (dishName != null && !dishName.isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + dishName + "%");
            }

            preparedStatement.setInt(paramIndex++, size);
            preparedStatement.setInt(paramIndex, (page - 1) * size);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Dish dish = new Dish(
                            resultSet.getInt("dish_id"),
                            resultSet.getString("dish_name"),
                            DishTypeEnum.valueOf(resultSet.getString("dish_type"))
                    );

                    Double requiredQuantity = null;
                    Object qtyObject = resultSet.getObject("required_quantity");
                    if (qtyObject != null) {
                        requiredQuantity = resultSet.getDouble("required_quantity");
                    }

                    Ingredient ingredient = new Ingredient(
                            resultSet.getInt("ingredient_id"),
                            resultSet.getString("ingredient_name"),
                            resultSet.getDouble("price"),
                            CategoryEnum.valueOf(resultSet.getString("category")),
                            dish,
                            requiredQuantity
                    );

                    ingredientList.add(ingredient);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ingredients by criteria: " + e.getMessage(), e);
        }

        return ingredientList;
    }
}
