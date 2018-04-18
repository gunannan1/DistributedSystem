# Telnet commands
{"command":"REGISTER","username":"ningk1","secret":"abc"}
{"command":"REGISTER","username":"asdfasdfasdfa","secret":"abc"}
{"command":"REGISTER","username":"ningk3","secret":"abc"}

{"command":"REGISTER","username":"ningk3"}

{"command":"LOGIN","username":"kangnwh","secret":"abc"}
{"command":"LOGIN","username":"anonymous"}

{"command":"LOGIN","username":"ningk1","secret":"abc"}
{"command":"LOGIN","username":"ningk2","secret":"abc"}
{"command":"LOGIN","username":"ningk3","secret":"abc"}


{"command":"AUTHENTICATE","secret":"gen1p85md2qnq0d59qll3fbcoa"}
{"command":"LOCK_REQUEST","username":"adsfasdfasdf","secret":"asd1234asdf"}
{"command":"LOCK_ALLOWED","username":"adsfasdfasdf","secret":"asd1234asdf"}

{"command":"AUTHENTICATE","secret":"gen1p85md2qnq0d59qll3fbcoa"}
{"command":"LOCK_ALLOWED","username":"adsfasdfasdf","secret":"asd1234asdf","server":"asdfasdfqwefawe"}


#### test case - user register with multi servers
# start the very first server
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8001 -s abc
# New servers joining the system
# Connect to 8001 server with system secret
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001

# Connect to 8001 server with system secret
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001 

# Connect to 8002 server with system secret
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8004 -s abc -rh localhost -rp 8002

# Connect to 8004 server with system secret
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8005 -s abc -rh localhost -rp 8004

# Connect to 8004 server with system secret
java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8006 -s abc -rh localhost -rp 8004 


# Register user
# Register user named 'ningk' at server 8001
java -jar Client-jar-with-dependencies.jar -r -u ningk -rp 8001 -rh localhost -s secret1

# Register user named 'yirupan' at server 8002
java -jar Client-jar-with-dependencies.jar -r -u yirupan -rp 8002 -rh localhost -s secret1

# Register user named 'nannangu' at server 8002
java -jar Client-jar-with-dependencies.jar -r -u nannangu -rp 8002 -rh localhost -s secret1

# Register user named 'wenyizhao' at server 8005
java -jar Client-jar-with-dependencies.jar -r -u wenyizhao -rp 8005 -rh localhost -s secret1

# User Login
# Login user named 'ningk' at server 8003 (instead of 8001 which this id registers at)
java -jar Client-jar-with-dependencies.jar -l -u ningk -rp 8003 -rh localhost -s secret1

# Login user named 'yirupan' at server 8001 (instead of 8002 which this id registers at)
java -jar Client-jar-with-dependencies.jar -l -u yirupan -rp 8001 -rh localhost -s secret1

# Login user named 'nannangu' at server 8004 (instead of 8002 which this id registers at)
java -jar Client-jar-with-dependencies.jar -l -u nannangu -rp 8004 -rh localhost -s secret1

# Login user named 'wenyizhao' at server 8002 (instead of 8005 which this id registers at)
java -jar Client-jar-with-dependencies.jar -l -u wenyizhao -rp 8002 -rh localhost -s secret1



# Connect to teachers' server
java -jar Client-jar-with-dependencies.jar -r  -u kangnwh -rp 3781 -rh sunrise.cis.unimelb.edu.au -s abc
java -jar Client-jar-with-dependencies.jar -l  -u kangnwh -rp 3781 -rh sunrise.cis.unimelb.edu.au -s abc

java -jar Server-jar-with-dependencies.jar -lh localhost -lp 8006 -s 'gen1p85md2qnq0d59qll3fbcoa' -rh sunrise.cis.unimelb.edu.au -rp 3781
{"command": "AUTHENTICATE","secret":"gen1p85md2qnq0d59qll3fbcoa"}


java -jar Client-jar-with-dependencies.jar -r -u kangnwh -rp 8001 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -l -u kangnwh -rp 8003 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -r -u kangnwh -rp 8002 -rh localhost -s abc

java -jar Client-jar-with-dependencies.jar -r -u kangnwh1 -rp 8001 -rh localhost -s abc
java -jar Client-jar-with-dependencies.jar -l -u kangnwh1 -rp 8001 -rh localhost -s abc