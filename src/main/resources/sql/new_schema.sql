DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'unit_type') THEN
CREATE TYPE unit_type AS ENUM (
            'PCS',
            'KG',
            'L'
        );
RAISE NOTICE 'Type unit_type created successfully';
ELSE
        RAISE NOTICE 'Type unit_type already exists, no changes made';
END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'dish'
        AND column_name = 'price'
    ) THEN
        ALTER TABLE Dish
        ADD COLUMN price NUMERIC;

        RAISE NOTICE 'Column price added to Dish table';
    ELSE
        RAISE NOTICE 'Column price already exists in Dish table';
    END IF;
END $$;


CREATE TABLE IF NOT EXISTS DishIngredient (
    id SERIAL PRIMARY KEY,
    id_dish INT NOT NULL,
    id_ingredient INT NOT NULL,
    quantity_required NUMERIC NOT NULL,
    unit unit_type NOT NULL,
        CONSTRAINT fk_dishingredient_dish
            FOREIGN KEY (id_dish) REFERENCES Dish(id)
                ON DELETE CASCADE,
    CONSTRAINT fk_dishingredient_ingredient
        FOREIGN KEY (id_ingredient) REFERENCES Ingredient(id)
            ON DELETE CASCADE,
                CONSTRAINT unique_dish_ingredient UNIQUE (id_dish, id_ingredient)
);

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'ingredient'
              AND column_name = 'id_dish'
        ) THEN
            ALTER TABLE Ingredient DROP COLUMN id_dish;
            RAISE NOTICE 'Column id_dish dropped from Ingredient table';
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'ingredient'
              AND column_name = 'required_quantity'
        ) THEN
            ALTER TABLE Ingredient DROP COLUMN required_quantity;
            RAISE NOTICE 'Column required_quantity dropped from Ingredient table';
        END IF;
END $$;