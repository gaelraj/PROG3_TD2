import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {
    private final DBConnection dbConnection;

    public DataRetriever(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Dish findDishById(Integer id) {
        String dishQuery = """
            SELECT Dish.id as dish_id, Dish.name as dish_name, dish_type, price
            FROM Dish
            WHERE id = ?  
            """;

        String dishIngredientsQuery = """
             SELECT di.id as di_id, 
               di.id_dish, 
               di.id_ingredient, 
               di.quantity_required, 
               di.unit,
               i.id as ingredient_id, 
               i.name as ingredient_name, 
               i.price, 
               i.category
        FROM DishIngredient di
        JOIN Ingredient i ON di.id_ingredient = i.id
        WHERE di.id_dish = ?
            """;

        try (Connection connection = dbConnection.getDBConnection()) {

            Dish dish = null;
            try (PreparedStatement preparedStatement = connection.prepareStatement(dishQuery)) {
                preparedStatement.setInt(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {

                        Double price = null;
                        Object priceObj = resultSet.getObject("price");

                        if (priceObj != null) {
                            price = resultSet.getDouble("price");
                        }

                        dish = new Dish();
                        dish.setId(resultSet.getInt("dish_id"));
                        dish.setName(resultSet.getString("dish_name"));
                        dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));

                        dish.setPrice(price);

                    } else {
                        throw new RuntimeException("Dish not found");
                    }
                }
            }

            List<DishIngredient> dishIngredients = new ArrayList<>();

            try (PreparedStatement dishIngredientsStmt = connection.prepareStatement(dishIngredientsQuery)) {
                dishIngredientsStmt.setInt(1, id);

                try (ResultSet dishIngredientsRs = dishIngredientsStmt.executeQuery()) {

                    while (dishIngredientsRs.next()) {

                        Double requiredQuantity = null;
                        Object qtyObj = dishIngredientsRs.getObject("quantity_required");
                        if (qtyObj != null) {
                            requiredQuantity = dishIngredientsRs.getDouble("quantity_required");
                        }

                        DishIngredient dishIng = new DishIngredient();
                        dishIng.setId(dishIngredientsRs.getInt("di_id"));
                        dishIng.setDishId(dishIngredientsRs.getInt("id_dish"));
                        dishIng.setIngredientId(dishIngredientsRs.getInt("id_ingredient"));
                        dishIng.setQuantity_required(requiredQuantity);
                        dishIng.setUnit(UnitEnum.valueOf(dishIngredientsRs.getString("unit")));

                        Ingredient ingredient = new Ingredient();
                        ingredient.setId(dishIngredientsRs.getInt("ingredient_id"));
                        ingredient.setName(dishIngredientsRs.getString("ingredient_name"));
                        ingredient.setPrice(dishIngredientsRs.getDouble("price"));
                        ingredient.setCategory(CategoryEnum.valueOf(dishIngredientsRs.getString("category")));

                        dishIng.setIngredient(ingredient);
                        dishIng.setDish(dish);

                        dishIngredients.add(dishIng);
                    }
                }
            }

            dish.setDishIngredients(dishIngredients);

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    public Ingredient findIngredientById(Integer id) {
        String ingredientQuery =
                """
                SELECT i.id AS ingredient_id,
                       i.name AS ingredient_name,
                       i.price AS ingredient_price,
                       i.category
                FROM Ingredient i 
                WHERE i.id = ?
                """;

        String stockMovementsQuery =
                """        
                SELECT sm.id as sm_id,
                    sm.id_ingredient,
                    sm.quantity,
                    sm.type,
                    sm.unit,
                    sm.creation_datetime
                FROM stockmovement sm
                WHERE sm.id_ingredient = ?
                ORDER BY sm.creation_datetime DESC
                """;

        if (id == null) {
            throw new RuntimeException("id must not be null");
        };

        try (Connection connection = dbConnection.getDBConnection()) {

            Ingredient ingredient = null;

            try (PreparedStatement preparedStatement = connection.prepareStatement(ingredientQuery)) {
                preparedStatement.setInt(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {

                        Double price = null;
                        Object priceObj = resultSet.getObject("ingredient_price");
                        if (priceObj != null) {
                            price = resultSet.getDouble("ingredient_price");
                        }

                        ingredient = new Ingredient();
                        ingredient.setId(resultSet.getInt("ingredient_id"));
                        ingredient.setName(resultSet.getString("ingredient_name"));
                        ingredient.setPrice(price);
                        ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

                    } else {
                        throw new RuntimeException("Ingredient not found");
                    }
                }

                List<StockMovement> stockMovements = new ArrayList<>();
                try (PreparedStatement stockMovementsStmt = connection.prepareStatement(stockMovementsQuery)) {
                    stockMovementsStmt.setInt(1, id);
                    try (ResultSet stockMovementsRs = stockMovementsStmt.executeQuery()) {
                        while (stockMovementsRs.next()) {

                            Double quantity = null;
                            Object qtyObj = stockMovementsRs.getObject("quantity");
                            if (qtyObj != null) {
                                quantity = stockMovementsRs.getDouble("quantity");
                            }

                            StockValue stockValue = new StockValue();
                            stockValue.setQuantity(quantity);
                            stockValue.setUnit(UnitEnum.valueOf(stockMovementsRs.getString("unit")));

                            StockMovement movement = new StockMovement();
                            movement.setId(stockMovementsRs.getInt("sm_id"));
                            movement.setValue(stockValue);
                            movement.setType(MovementTypeEnum.valueOf(stockMovementsRs.getString("type")));
                            movement.setCreationDatetime(stockMovementsRs.getTimestamp("creation_datetime").toInstant());

                            stockMovements.add(movement);
                        }
                    }
                }

                ingredient.setStockMovementList(stockMovements);

                return ingredient;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = """
                SELECT Ingredient.id as ingredient_id, 
                   Ingredient.name as ingredient_name, 
                   Ingredient.price as ingredient_price, 
                   Ingredient.category
                FROM Ingredient
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

                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(resultSet.getInt("ingredient_id"));
                    ingredient.setName(resultSet.getString("ingredient_name"));
                    ingredient.setPrice(resultSet.getDouble("ingredient_price"));
                    ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

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
        String insertionQuery = "INSERT INTO ingredient (id, name, price, category) VALUES (?, ?, ?, ?::category)";

        try (Connection connection = dbConnection.getDBConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement checkStmt = connection.prepareStatement(checkingQuery);
                 PreparedStatement insertStmt = connection.prepareStatement(insertionQuery)) {

                for (Ingredient ingredient : newIngredients) {

                    checkStmt.setString(1, ingredient.getName());

                    try (ResultSet resultSet = checkStmt.executeQuery()) {
                        resultSet.next();
                        int count = resultSet.getInt(1);
                        if (count > 0) {
                            throw new RuntimeException("Ingredient already exists: " + ingredient.getName());
                        }
                    }

                    insertStmt.setInt(1, ingredient.getId());
                    insertStmt.setString(2, ingredient.getName());
                    insertStmt.setDouble(3, ingredient.getPrice());
                    insertStmt.setString(4, ingredient.getCategory().name());

                    insertStmt.executeUpdate();
                    savedIngredients.add(ingredient);
                }

                connection.commit();
                return savedIngredients;

            } catch (Exception e) {
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
        String insertDishSql = "INSERT INTO dish (id, name, dish_type, price) VALUES (?, ?, ?::dish_type, ?)";
        String updateDishSql = "UPDATE dish SET name = ?, dish_type = ?::dish_type, price = ? WHERE id = ?";
        String deleteDishIngredientsSql = "DELETE FROM DishIngredient WHERE id_dish = ?";
        String insertDishIngredientSql = "INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit) VALUES (?, ?, ?, ?::unit_type)";

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

                        if (dishToSave.getPrice() != null) {
                            insertStmt.setDouble(4, dishToSave.getPrice());
                        } else {
                            insertStmt.setNull(4, java.sql.Types.NUMERIC);
                        }

                        insertStmt.executeUpdate();
                    }
                    System.out.println("Dish created with ID: " + dishToSave.getId());
                } else {
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateDishSql)) {
                        updateStmt.setString(1, dishToSave.getName());
                        updateStmt.setString(2, dishToSave.getDishType().name());

                        if (dishToSave.getPrice() != null) {
                            updateStmt.setDouble(3, dishToSave.getPrice());
                        } else {
                            updateStmt.setNull(3, java.sql.Types.NUMERIC);
                        }

                        updateStmt.setInt(4, dishToSave.getId());
                        updateStmt.executeUpdate();
                    }
                    System.out.println("Dish updated with ID: " + dishToSave.getId());
                }

                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteDishIngredientsSql)) {
                    deleteStmt.setInt(1, dishToSave.getId());
                    deleteStmt.executeUpdate();
                }

                if (dishToSave.getDishIngredients() != null && !dishToSave.getDishIngredients().isEmpty()) {
                    try (PreparedStatement insertDishIngStmt = connection.prepareStatement(insertDishIngredientSql)) {
                        for (DishIngredient dishIng : dishToSave.getDishIngredients()) {
                            insertDishIngStmt.setInt(1, dishToSave.getId());
                            insertDishIngStmt.setInt(2, dishIng.getIngredientId() );

                            if (dishIng.getQuantity_required() != null) {
                                insertDishIngStmt.setDouble(3, dishIng.getQuantity_required());
                            } else {
                                insertDishIngStmt.setNull(3, java.sql.Types.NUMERIC);
                            }

                            if (dishIng.getUnit() != null) {
                                insertDishIngStmt.setString(4, dishIng.getUnit().name());
                            } else {
                                insertDishIngStmt.setString(4, "PCS");
                            }

                            insertDishIngStmt.executeUpdate();
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

    public List<Dish> findDishByIngredientName(String ingredientName) {
        String query = """
                        SELECT d.id AS dish_id, d.name AS dish_name, d.dish_type,
		d.price AS dish_price,
		i.id AS ingredient_id, i.name AS ingredient_name,
		i.price AS ingredient_price,
		i.category AS ingredient_category,
		di.id AS di_id,
		di.quantity_required,
		di.unit
		FROM DishIngredient di
		INNER JOIN Dish d ON di.id_dish = d.id
		INNER JOIN Ingredient i ON di.id_ingredient = i.id 
		WHERE i.name ILIKE ?
		ORDER BY d.id;
      """;

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
                        dishCourant = new Dish();
                        dishCourant.setId(dish_id);
                        dishCourant.setName(resultSet.getString("dish_name"));
                        dishCourant.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                        dishCourant.setPrice(resultSet.getDouble("dish_price"));

                        dishList.add(dishCourant);
                    }
                    
                    DishIngredient dishIng = new DishIngredient();
                    dishIng.setId(resultSet.getInt("di_id"));
                    dishIng.setDishId(resultSet.getInt("dish_id"));
                    dishIng.setIngredientId(resultSet.getInt("ingredient_id"));
                    dishIng.setQuantity_required(resultSet.getDouble("quantity_required"));
                    dishIng.setUnit(UnitEnum.valueOf(resultSet.getString("unit")));


                    dishCourant.getDishIngredients().add(dishIng);
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
                """
                SELECT d.id AS dish_id, d.name AS dish_name, d.dish_type,
                d.price AS dish_price,
                i.id AS ingredient_id, i.name AS ingredient_name,
                i.price AS ingredient_price,
                i.category AS ingredient_category,
                di.quantity_required,
                di.unit
                FROM DishIngredient di
                INNER JOIN Dish d ON di.id_dish = d.id
                INNER JOIN Ingredient i ON di.id_ingredient = i.id
                WHERE 1=1
                """
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
                            DishTypeEnum.valueOf(resultSet.getString("dish_type")),
                            resultSet.getDouble("dish_price")
                    );

                    Double requiredQuantity = null;
                    Object qtyObject = resultSet.getObject("quantity_required");
                    if (qtyObject != null) {
                        requiredQuantity = resultSet.getDouble("quantity_required");
                    }

                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(resultSet.getInt("ingredient_id"));
                    ingredient.setName(resultSet.getString("ingredient_name"));
                    ingredient.setPrice(resultSet.getDouble("ingredient_price"));
                    CategoryEnum.valueOf(resultSet.getString("ingredient_category"));

                    ingredientList.add(ingredient);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ingredients by criteria: " + e.getMessage(), e);
        }

        return ingredientList;
    };

    public List<DishIngredient> addIngredientToDish(List<DishIngredient> dishIngredients) {

        if (dishIngredients == null || dishIngredients.isEmpty()) {
            throw new IllegalArgumentException("Dish ingredients must not be null or empty");
        }

        String insertQuery =
                """
                    INSERT INTO dishingredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES (?, ?, ?, ?, ?::unit_type);
                """;

        List<DishIngredient> dishIngredientList = new ArrayList<>();

        try (Connection connection = dbConnection.getDBConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            for (DishIngredient dishIngredient : dishIngredients) {

                preparedStatement.setInt(1, dishIngredient.getId());
                preparedStatement.setInt(2, dishIngredient.getDishId());
                preparedStatement.setInt(3, dishIngredient.getIngredientId());
                preparedStatement.setDouble(4, dishIngredient.getQuantity_required());
                preparedStatement.setString(5, dishIngredient.getUnit().name());

                preparedStatement.executeUpdate();
                dishIngredientList.add(dishIngredient);
            }

            return dishIngredientList;

        } catch (SQLException e) {
            throw new RuntimeException("Error adding ingredient: " + e.getMessage(), e);
        }

    };

    public Ingredient saveIngredient(Ingredient toSave) {
      String upsertIngredientSql =
              """
                INSERT INTO ingredient (id, name, price, category)
                VALUES (?, ?, ?, ?::ingredient_category)
                ON CONFLIT (id) DO UPDATE
                SET name = EXCLUDED.name;
                    category = ExCLUDED.category;
                    price = EXCLUDED.price;
                
                RETURNING id;
              """;

      try (Connection connection = dbConnection.getDBConnection()) {

          connection.setAutoCommit(false);
          Integer ingredientId;

          try (PreparedStatement ps = connection.prepareStatement(upsertIngredientSql)) {

              if (toSave.getId() != null) {
                  ps.setInt(1 , toSave.getId());
              } else {
                  String getNextIdQuery = "SELECT COALESCE(MAX(id), 0) + 1 FROM ingredient";
                  try (PreparedStatement nextIdStmt = connection.prepareStatement(getNextIdQuery);
                       ResultSet rs = nextIdStmt.executeQuery()
                  ) {
                      if (rs.next()) {
                          ps.setInt(1, rs.getInt(1));
                      }
                  }
              }

              if (toSave.getPrice() != null) {
                  ps.setDouble(3, toSave.getPrice());
              } else {
                  ps.setNull(3, Types.DOUBLE);
              }

              ps.setString(2, toSave.getName());
              ps.setString(4, toSave.getCategory().name());

              try (ResultSet rs = ps.executeQuery()) {
                  rs.next();
                  ingredientId = rs.getInt(1);
              }
          }

          insertIngredientStockMovement(toSave);

          connection.commit();
          return findIngredientById(ingredientId);

      } catch (SQLException e) {
          throw new RuntimeException("Error saving ingredient: " + e.getMessage(), e);
      }

    };

    public void insertIngredientStockMovement(Ingredient ingredient) {
        List<StockMovement> stockMovementList = ingredient.getStockMovementList();
        String sql =
                """
                    INSERT INTO stockmovement (id, id_ingredient, quantity, type, unit, creation_datetime)
                    VALUES (?,? ,? , ?::movement_type, ?::unit_type, ?)
                    ON CONFLIT (id) DO NOTHING
                """;

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            for (StockMovement stockMovement : stockMovementList) {

                if (stockMovement.getId() != null) {
                    preparedStatement.setInt(1, stockMovement.getId());
                } else {

                    String getNextIdQuery = "SELECT COALESCE(MAX(id), 0) + 1 FROM stockmovement";
                    try (PreparedStatement nextIdStmt = connection.prepareStatement(getNextIdQuery);
                         ResultSet rs = nextIdStmt.executeQuery()) {
                        if (rs.next()) {
                            preparedStatement.setInt(1, rs.getInt(1));
                        }
                    }
                }
                preparedStatement.setInt(2,ingredient.getId());
                preparedStatement.setDouble(3,stockMovement.getValue().getQuantity());
                preparedStatement.setObject(4, stockMovement.getType());
                preparedStatement.setObject(5, stockMovement.getValue().getUnit());
                preparedStatement.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting stock movement: " + e.getMessage(), e);
        }
    };

    private List<DishOrder> findDishOrderByIdOrder(Integer idOrder) {

        String query = """
                    SELECT id, id_dish, quantity 
                    FROM DishOrder
                    WHERE DishOrder.id_order = ? 
                """;
        List<DishOrder> dishOrders = new ArrayList<>();

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {

            preparedStatement.setInt(1, idOrder);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Dish dish = findDishById(resultSet.getInt("id_dish"));
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(resultSet.getInt("id"));
                dishOrder.setDish(dish);
                dishOrder.setQuantity(resultSet.getInt("quantity"));
                dishOrders.add(dishOrder);
            }

            return dishOrders;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    public Order findOrderByReference(String reference) {

        String getReferenceQuery =
                """
                    SELECT id, reference, creation_datetime
                    FROM "order"
                    WHERE reference like ?        
                """;

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getReferenceQuery)
        ) {
            preparedStatement.setString(1, reference);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    Integer orderId = rs.getInt("id");
                    order.setId(orderId);
                    order.setReference(rs.getString("reference"));
                    order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                    order.setDishOrderList(findDishOrderByIdOrder(orderId));
                    return order;
                }

                throw new RuntimeException("No order found with reference: " + reference);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };
};