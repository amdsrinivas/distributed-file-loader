CREATE TABLE IF NOT EXISTS PRODUCTS_FCT(
    PRODUCT_ID INTEGER PRIMARY KEY,
    PRODUCT_NAME VARCHAR(100),
    PRODUCT_SKU VARCHAR(100),
    PRODUCT_DESCRIPTION VARCHAR(500),
    LINE_NUMBER INTEGER,
    UPDATED_ON_RUN INTEGER
) ;