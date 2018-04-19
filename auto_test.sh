### Telnet commands
##{"command":"REGISTER","username":"ningk1","secret":"abc"}
##{"command":"REGISTER","username":"asdfasdfasdfa","secret":"abc"}
##{"command":"REGISTER","username":"ningk3","secret":"abc"}
##
##{"command":"REGISTER","username":"ningk3"}
##{"command":"REGISTER","username":"ningk3","secret":null}
##
##
##{"command":"LOGIN","username":"anonymous","secret":"abc"}
##{"command":"LOGIN","username":"anonymous"}
##{"command":"LOGIN","username":"anonymous","secret":""}
##{"command":"LOGIN","username":"anonymous","secret":null}
##
##{"command":"LOGIN","username":"kadddddngnwh"}
##
##{"command":"LOGIN","secret":"abc"}
##
##
##{"command":"LOGIN","username":"ningk1","secret":"abc"}
##{"command":"LOGIN","username":"ningk2","secret":"abc"}
##{"command":"LOGIN","username":"ningk3","secret":"abc"}
##
##
##{"command":"AUTHENTICATE","secret":"gen1p85md2qnq0d59qll3fbcoa"}
##{"command":"LOCK_REQUEST","username":"adsfasdfasdf","secret":"asd1234asdf"}
##{"command":"LOCK_ALLOWED","username":"adsfasdfasdf","secret":"asd1234asdf"}
##
##{"command":"AUTHENTICATE","secret":"gen1p85md2qnq0d59qll3fbcoa"}
##{"command":"LOCK_ALLOWED","username":"adsfasdfasdf","secret":"asd1234asdf","server":"asdfasdfqwefawe"}



#注册server

nohup java -jar target/Server-jar-with-dependencies.jar -lh localhost -lp 8001 -s abc > log/8001.log &
sleep 3
nohup java -jar target/Server-jar-with-dependencies.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001 > log/8002.log 2>&1 &
sleep 3
nohup java -jar target/Server-jar-with-dependencies.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001 > log/8003.log 2>&1 &
sleep 3
nohup java -jar target/Server-jar-with-dependencies.jar -lh localhost -lp 8004 -s abc -rh localhost -rp 8002 > log/8004.log 2>&1 &
sleep 3
nohup java -jar target/Server-jar-with-dependencies.jar -lh localhost -lp 8005 -s abc -rh localhost -rp 8004 > log/8004.log 2>&1 &
sleep 3
nohup java -jar target/Server-jar-with-dependencies.jar -lh localhost -lp 8006 -s abc -rh localhost -rp 8004 > log/8004.log 2>&1 &
sleep 3

#注册client
java -jar target/Client-jar-with-dependencies.jar -r -u yirupan -rp 8001 -rh localhost -s s_a
java -jar target/Client-jar-with-dependencies.jar -r -u wenyizhao -rp 8002 -rh localhost -s s_b
java -jar target/Client-jar-with-dependencies.jar -r -u nanangu -rp 8004 -rh localhost -s s_c
java -jar target/Client-jar-with-dependencies.jar -r -u ningk -rp 8003 -rh localhost -s s_d

#登陆client
nohup java -jar target/Client-jar-with-dependencies.jar -l -u yirupan -rp 8001 -rh localhost -s s_a > log/client1.log 2>&1 &
nohup java -jar target/Client-jar-with-dependencies.jar -l -u wenyizhao -rp 8001 -rh localhost -s s_b > log/client2.log 2>&1 &
nohup java -jar target/Client-jar-with-dependencies.jar -l -u nanangu -rp 8001 -rh localhost -s s_c > log/client3.log 2>&1 &
nohup java -jar target/Client-jar-with-dependencies.jar -l -u ningk -rp 8004 -rh localhost -s s_d > log/client4.log 2>&1 &