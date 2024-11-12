@echo off
cd /d %~dp0
java -cp ./lib/gson-2.11.0.jar;./lib/mysql-connector-j-9.1.0.jar Server.java
pause