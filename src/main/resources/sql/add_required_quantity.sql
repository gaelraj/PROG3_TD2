DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'ingredient'
        AND column_name = 'required_quantity'
    ) THEN
ALTER TABLE Ingredient
    ADD COLUMN required_quantity NUMERIC;

RAISE NOTICE 'Column required_quantity added successfully';
ELSE
        RAISE NOTICE 'Column required_quantity already exists, no changes made';
END IF;
END $$;

UPDATE Ingredient SET required_quantity = 1 WHERE name = 'Laitue';
UPDATE Ingredient SET required_quantity = 2 WHERE name = 'Tomate';
UPDATE Ingredient SET required_quantity = 0.5 WHERE name = 'Poulet';
UPDATE Ingredient SET required_quantity = NULL WHERE name = 'Chocolat';
UPDATE Ingredient SET required_quantity = NULL WHERE name = 'Beurre';

SELECT
    id,
    name,
    required_quantity,
    category
FROM Ingredient
ORDER BY name;
