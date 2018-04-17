{"command":"REGISTER","username":"ningk1","secret":"abc"}
{"command":"REGISTER","username":"ningk2","secret":"abc"}
{"command":"REGISTER","username":"ningk3","secret":"abc"}

{"command":"REGISTER","username":"ningk3"}

{"command":"LOGIN","username":"kangnwh","secret":"abc"}
{"command":"LOGIN","username":"anonymous"}

{"command":"LOGIN","username":"ningk1","secret":"abc"}
{"command":"LOGIN","username":"ningk2","secret":"abc"}
{"command":"LOGIN","username":"ningk3","secret":"abc"}


#### test case - user register with multi servers
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8001 -s abc
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001

java -jar Client-jar-with-dependencies.jar -r -u kangnwh -rp 8001 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -l -u kangnwh -rp 8003 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -r -u kangnwh -rp 8002 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -r -u kangnwh1 -rp 8001 -rh localhost -s abc
java -jar Client-jar-with-dependencies.jar -l -u kangnwh1 -rp 8001 -rh localhost -s abc