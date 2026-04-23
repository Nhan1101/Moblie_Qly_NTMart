DROP TRIGGER IF EXISTS trg_sale_ticket_details_insert;
DROP TRIGGER IF EXISTS trg_sale_ticket_details_update;
DROP TRIGGER IF EXISTS trg_sale_ticket_details_delete;

CREATE TRIGGER trg_sale_ticket_details_insert
AFTER INSERT ON sale_ticket_details
FOR EACH ROW
BEGIN
    SELECT
        CASE
            WHEN (SELECT stock_quantity FROM products WHERE id = NEW.product_id) < NEW.quantity
            THEN RAISE(ABORT, 'Khong du ton kho')
        END;

    UPDATE products
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE id = NEW.product_id;
END;

CREATE TRIGGER trg_sale_ticket_details_update
AFTER UPDATE ON sale_ticket_details
FOR EACH ROW
BEGIN
    UPDATE products
    SET stock_quantity = stock_quantity + OLD.quantity
    WHERE id = OLD.product_id;

    SELECT
        CASE
            WHEN (SELECT stock_quantity FROM products WHERE id = NEW.product_id) < NEW.quantity
            THEN RAISE(ABORT, 'Khong du ton kho')
        END;

    UPDATE products
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE id = NEW.product_id;
END;

CREATE TRIGGER trg_sale_ticket_details_delete
AFTER DELETE ON sale_ticket_details
FOR EACH ROW
BEGIN
    UPDATE products
    SET stock_quantity = stock_quantity + OLD.quantity
    WHERE id = OLD.product_id;
END;
