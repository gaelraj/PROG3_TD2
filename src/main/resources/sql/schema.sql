CREATE TYPE category AS ENUM (
    'VEGETABLE',
    'ANIMAL',
    'MARINE',
    'DAIRY',
    'OTHER'
);

CREATE TYPE dish_type AS ENUM (
    'START',
    'MAIN',
    'DESSERT'
);

CREATE TABLE Dish (
    id INT PRIMARY KEY,
    name VARCHAR,
    dish_type dish_type
);

CREATE TABLE Ingredient (
    id INT PRIMARY KEY,
    name VARCHAR,
    price NUMERIC,
    category category,
    id_dish INT,
    CONSTRAINT fk_ingredient_dish
        FOREIGN KEY (id_dish) REFERENCES Dish(id)
);
