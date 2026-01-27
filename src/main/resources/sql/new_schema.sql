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

    IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = 'dish_id_seq') THEN
        CREATE SEQUENCE dish_id_seq;
        ALTER TABLE Dish ALTER COLUMN id SET DEFAULT nextval('dish_id_seq');
        ALTER SEQUENCE dish_id_seq OWNED BY Dish.id;
        PERFORM setval('dish_id_seq', COALESCE((SELECT MAX(id) FROM Dish), 0) + 1, false);
        RAISE NOTICE 'ID Dish configured to auto-incrément (SERIAL)';
    END IF;

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

        IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = 'ingredient_id_seq') THEN
            CREATE SEQUENCE ingredient_id_seq;
            ALTER TABLE Ingredient ALTER COLUMN id SET DEFAULT nextval('ingredient_id_seq');
            ALTER SEQUENCE ingredient_id_seq OWNED BY Ingredient.id;
            PERFORM setval('ingredient_id_seq', COALESCE((SELECT MAX(id) FROM Ingredient), 0) + 1, false);
            RAISE NOTICE 'ID Ingredient configured to auto-incrément (SERIAL)';
        ELSE
            RAISE NOTICE 'Sequence ingredient_id_seq already exists';
        END IF;

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

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'movement_type') THEN
            CREATE TYPE movement_type AS ENUM ('IN', 'OUT');
            RAISE NOTICE 'Type movement_type created successfully';
        ELSE
            RAISE NOTICE 'Type movement_type already exists';
        END IF;
    END $$;

CREATE TABLE IF NOT EXISTS StockMovement (
    id SERIAL PRIMARY KEY,
    id_ingredient INT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL,
    type movement_type NOT NULL,
    unit unit_type NOT NULL,
    creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_stockmovement_ingredient
        FOREIGN KEY (id_ingredient) REFERENCES Ingredient(id)
            ON DELETE CASCADE,

    CONSTRAINT check_quantity_positive CHECK (quantity > 0)
);