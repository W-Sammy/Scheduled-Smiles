@echo off
cd /d %~dp0..\..
java -cp ./backend/Server/lib/gson-2.11.0.jar;./backend/Server/lib/mysql-connector-j-9.1.0.jar ./backend/Server/Server.java "./frontend/"
pause