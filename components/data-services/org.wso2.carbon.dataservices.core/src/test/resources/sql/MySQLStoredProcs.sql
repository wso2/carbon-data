CREATE PROCEDURE getCustomerInfo() SELECT customerNumber, customerName, contactLastName, phone, city FROM Customers;

CREATE PROCEDURE getCustomerInfoWithId(custNo INTEGER) SELECT customerNumber, customerName, contactLastName, phone, city FROM Customers WHERE customerNumber = custNo;

CREATE PROCEDURE getCustomerInfoWithIdLastName(custNo INTEGER, custLastName varchar(50)) SELECT customerNumber, customerName, contactLastName, phone, city FROM Customers WHERE customerNumber = custNo and contactLastName = custLastName;

CREATE PROCEDURE getCustomerCreditLimitWithId(custNo INTEGER) SELECT * FROM Customers WHERE customerNumber=custNo;

CREATE PROCEDURE getPaymentInfo() SELECT customerNumber, checkNumber, paymentDate, amount FROM Payments WHERE customerNumber is NOT NULL;

DELIMITER //

CREATE PROCEDURE get103CustomerLim(OUT custNo INTEGER, OUT custName VARCHAR(50))
    BEGIN
	    SET custNo=103;
        SELECT customerName INTO custName FROM Customers WHERE customerNumber=custNo;      
    END //
    
CREATE PROCEDURE get103CustomerFull(OUT custNo INTEGER, OUT custName VARCHAR(50))
    BEGIN
	    SET custNo=103;
        SELECT customerName INTO custName FROM Customers WHERE customerNumber=custNo;
        SELECT contactLastName, phone, city FROM Customers WHERE customerNumber = custNo;
    END //

CREATE PROCEDURE getCustomerFullWithNumber(INOUT custNo INTEGER, OUT custName VARCHAR(50))
    BEGIN
	    SELECT customerName INTO custName FROM Customers WHERE customerNumber=custNo;
	    SELECT customerNumber INTO custNo FROM Customers WHERE customerNumber=custNo;
        SELECT contactLastName, phone, city FROM Customers WHERE customerNumber = custNo;
    END //
DELIMITER ;
