#
# The utility nmap must be installed and in your path for this script to run.
# Download nmap for *nix and windows here:  http://nmap.org/dist/	
# 
echo 
echo 
echo   --------------------------------
echo   Expected output for running tests:
echo 
echo       9123/tcp open unknown
echo       9124/tcp open unknown
echo       9125/tcp open unknown
echo 
echo   Expected output for stopped tests:
echo 
echo       9123/tcp closed unknown
echo       9124/tcp closed unknown
echo       9125/tcp closed unknown
echo 
echo   --------------------------------
echo   Actual output:
echo   --------------------------------
echo 
nmap -sT localhost -p 9123 | grep tcp
nmap -sT localhost -p 9124 | grep tcp
nmap -sT localhost -p 9125 | grep tcp
echo
