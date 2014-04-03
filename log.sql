

-- 2013-11-11
	
	alter table Employee alter jobLevel set default 10;
	alter table BusinessClient add unique (clientNO);
	
	
-- 2013-11-22

	drop table EmployeeSDOrder;
	drop table WHMaterialBill;
	rename table BusinessClient to Client;


-- 2013-11-28

	alter table approvals change username employeeNO varchar(255);


-- 2013-12-06
	
	DROP TABLE DEPARTMENTS;
	
	
-- 2013-12-06

	alter table approvals drop column apnsToken;
	
	
-- 2013-12-06

	alter table employee drop column levelApp_1, drop column levelApp_2, drop column levelApp_3, drop column levelApp_4, drop column levelApp_5;
	alter table employee change departmentName department varchar(255), change subDepartmentName subDepartment varchar(255);
	
	alter table [table_name] change levelApp_1 app1 varchar(255), change levelApp_2 app2 varchar(255), change levelApp_3 app3 varchar(255), change levelApp_4 app4 varchar(255);
	
	
-- 2013-12-24

	rename table EmployeeDormitoryOrder to EmployeeDormOrder;


-- 2013-12-27

	alter table Employee alter inVisits set default false;
	alter table employee drop inVisitList, drop drivingLicence;
	
	
-- 2013-12-30

	drop table APPOrderAttributes;	

-- 2014-1-5

	drop table WHLendOutBill;
	drop table WHLendOutOrder;
	drop table WHInventoryOrder;
	drop table WHScrapOrder;
   
-- 2013-1-13

	alter table employee drop column photopath;
	
	
-- 2013-1-15

	alter table employee drop column orderNO, drop expiredDate, drop exception;

-- 2014-1-16

	drop table WHLendOutBill;
	drop table WHLendOutOrder;
	drop table WHInventoryOrder;
	drop table WHScrapOrder;
   
-- 2014-1-17

	drop table Client;

-- 2014-1-21

	alter table WHLendOutOrder drop column notReturnAmount;
	
	
-- 2014-1-17 Note: drop bill first to avoid foreign key constraint exception.
 	
 	drop table SharedReleaseBill;
	drop table SharedReleaseOrder;
	
-- 2014-1-21

	drop table WHLendOutBill;
	drop table WHProductCategory;
	drop table WHMaterialOrder;

-- 2014-2-26

	drop table FinancePettyCashOrder;
	
-- 2014-3-3
	
	alter table EmployeeQuitOrder drop column carryingStuff;
  
-- 2014-3-7

	drop table FinanceCHOrder;
	drop table FinanceSalaryOrder;
	drop table EmployeeBMOrder;
	
-- 2014-3-12

	alter table FinancePaymentOrder drop column biilOrderNOs;
	alter table WHPurchaseBill drop column purchaseQCBill;
	
-- 2014-3-14

	alter table WHPurchaseOrder change payable totalPay float NOT NULL;
	

-- 2014-3-18

	alter table WHInventoryCHOrder drop column totalAmount_N;
	alter table WHInventoryCHOrder drop column amount_N;
	alter table WHInventoryCHOrder drop column lendAmount_N;
	alter table WHInventoryCHOrder drop column priceBasicUnit_N;

-- 2014-3-19

	alter table FinancePaymentBill drop column orderType;
  
-- 2014-3-19

	alter table WHInventoryOrder drop column remainAmount;
	
-- 2014-3-24
	
	drop table FinanceAccountCHOrder;
	alter table FinanceAccount change column category bank varchar(255);

-- 2014-3-29

	drop table SecurityVisitOrder;
	
-- 2014-3-27
	
	use ERPWebServer;
	set @DATABASE_NAME = 'ERPWebServer';
	select concat('alter table ', table_name, ' drop column expiredDate;') as sql_statements from information_schema.tables as tb where table_schema = @DATABASE_NAME order by table_name DESC into outfile '/tmp/batch.txt'; 
	source /tmp/batch.txt;
	
-- 2014-3-29
	
	update appsettings set type='ADMIN_APPROVALS' where type='APPROVALS';
	UPDATE APPSETTINGS SET TYPE='USER_JOBPOSITIONS' WHERE TYPE='JOBPOSITIONS';
	
-- 2014-4-3

	update EmployeeCHOrder inner join Employee on EmployeeCHOrder.employeeNO = Employee.employeeNO set EmployeeCHOrder.name = Employee.name ;
	
	
	
	
	

