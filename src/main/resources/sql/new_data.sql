INSERT INTO DishIngredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES
    (1, 1, 1, 0.20, 'KG'),
    (2, 1, 2, 0.15, 'KG'),
    (3, 2, 3, 1.00, 'KG'),
    (4, 4, 4, 0.30, 'KG'),
    (5, 4, 5, 0.20, 'KG')
ON CONFLICT (id) DO UPDATE SET
    id_dish = EXCLUDED.id_dish,
    id_ingredient = EXCLUDED.id_ingredient,
    quantity_required = EXCLUDED.quantity_required,
    unit = EXCLUDED.unit;

UPDATE Dish SET price = 3500.00 WHERE id = 1;
UPDATE Dish SET price = 12000.00 WHERE id = 2;
UPDATE Dish SET price = NULL WHERE id = 3;
UPDATE Dish SET price = 8000.00 WHERE id = 4;
UPDATE Dish SET price = NULL WHERE id = 5;