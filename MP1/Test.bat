@echo off
title test, echo test
pause

echo This test needs the mp1.html file to be in the same folder as the java classes, otherwise all test cases will result in a 404 error
javac client.java
javac server.java
start cmd /k "java server"
echo -------------------------------------------------------------------------------
echo Executing: mp1.html
echo Expected: 200 OK
echo ------------------------
java client mp1.html
echo -------------------------------------------------------------------------------
echo Executing: mp2.html
echo Expected: 404 not found
echo ------------------------
java client mp2.html
echo -------------------------------------------------------------------------------
echo Executing: mp1.html mon, 12 May 2007 12:34:20 GMT
echo Expected: 200 OK
echo ------------------------
java client mp1.html mon, 12 May 2007 12:34:20 GMT
echo -------------------------------------------------------------------------------
echo Executing: mp1.html mon, 12 May 2027 12:34:20 GMT
echo Expected: 304 not modified
echo ------------------------
java client mp1.html mon, 12 May 2027 12:34:20 GMT
echo -------------------------------------------------------------------------------
echo Executing: mp2.html mon, 12 May 2007 12:34:20 GMT
echo Expected: 404 not found
echo ------------------------
java client mp2.html mon, 12 May 2007 12:34:20 GMT
echo -------------------------------------------------------------------------------
echo Executing: mp2.html mon, 12 May 2027 12:34:20 GMT
echo Expected: 404 not found
echo ------------------------
java client mp2.html mon, 12 May 2027 12:34:20 GMT
pause
