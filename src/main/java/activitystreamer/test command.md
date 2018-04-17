{"command":"REGISTER","username":"ningk1","secret":"abc"}
{"command":"REGISTER","username":"ningk2","secret":"abc"}
{"command":"REGISTER","username":"ningk3","secret":"abc"}

{"command":"REGISTER","username":"ningk3"}

{"command":"LOGIN","username":"kangnwh","secret":"abc"}
{"command":"LOGIN","username":"anonymous"}

{"command":"LOGIN","username":"ningk1","secret":"abc"}
{"command":"LOGIN","username":"ningk2","secret":"abc"}
{"command":"LOGIN","username":"ningk3","secret":"abc"}


".table th, .table td {padding: 0.75rem;  vertical-align: top;  border-top: 1px solid #dee2e6;}"

".table thead th {vertical-align: bottom;  border-bottom: 2px solid #dee2e6;}"

styleSheet.addRule(".table tbody + tbody { border-top: 2px solid #dee2e6;"}
styleSheet.addRule(".table .table { background-color: #fff;"}

lib/gson-2.2.2.jar lib/common-cli-1.3.1.jar lib/json-simple-1.1.jar lib/log4j-api-2.11.0.jar lib/log4j-core-2.11.0.jar


#### test case - user register with multi servers
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8001 -s abc
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001

java -jar Client-jar-with-dependencies.jar -r -u kangnwh -rp 8001 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -l -u kangnwh -rp 8003 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -r -u kangnwh -rp 8002 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -r -u kangnwh1 -rp 8001 -rh localhost -s abc
java -jar Client-jar-with-dependencies.jar -l -u kangnwh1 -rp 8001 -rh localhost -s abc