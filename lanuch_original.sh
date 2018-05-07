#mvn clean
#mvn package
#register server

nohup java -jar deliveriable/Server-original.jar -lh localhost -lp 8001 -s abc > log/8001.log &
sleep 3
nohup java -jar deliveriable/Server-original.jar -lh localhost -lp 8002 -s abc -rh localhost -rp 8001 > log/8002.log 2>&1 &
sleep 3
nohup java -jar deliveriable/Server-original.jar -lh localhost -lp 8003 -s abc -rh localhost -rp 8001 > log/8003.log 2>&1 &
sleep 3
nohup java -jar deliveriable/Server-original.jar -lh localhost -lp 8004 -s abc -rh localhost -rp 8002 > log/8004.log 2>&1 &
sleep 3
nohup java -jar deliveriable/Server-original.jar -lh localhost -lp 8005 -s abc -rh localhost -rp 8004 > log/8005.log 2>&1 &
sleep 3
nohup java -jar deliveriable/Server-original.jar -lh localhost -lp 8006 -s abc -rh localhost -rp 8004 > log/8006.log 2>&1 &
sleep 3

#register client
rm -f secret_map.csv
java -jar deliveriable/Client-original.jar -u yirupan -rp 8001 -rh localhost
java -jar deliveriable/Client-original.jar -u wenyizhao -rp 8002 -rh localhost
java -jar deliveriable/Client-original.jar -u nanangu -rp 8004 -rh localhost
java -jar deliveriable/Client-original.jar -u ningk -rp 8003 -rh localhost

#register client
secret=`grep yirupan secret_map.csv|cut -f 2 -d ',' `
nohup java -jar deliveriable/Client-original.jar -u yirupan -rp 8001 -rh localhost -s 2kk98drmsl2f5utd2ra0m08pu6 > log/yirupan.log 2>&1 &
sleep 3

secret=`grep wenyizhao secret_map.csv|cut -f 2 -d ',' `
nohup java -jar deliveriable/Client-original.jar -u wenyizhao -rp 8001 -rh localhost -s ${secret} > log/wenyizhao.log 2>&1 &
sleep 3

secret=`grep nanangu secret_map.csv|cut -f 2 -d ',' `
nohup java -jar deliveriable/Client-original.jar -u nanangu -rp 8001 -rh localhost -s ${secret} > log/nanangu.log 2>&1 &
sleep 3

secret=`grep ningk secret_map.csv|cut -f 2 -d ',' `
nohup java -jar deliveriable/Client-original.jar -u ningk -rp 8004 -rh localhost -s ${secret} > log/ningk.log 2>&1 &
sleep 3

secret=`grep yirupan secret_map.csv|cut -f 2 -d ',' `
nohup java -jar deliveriable/Client-original.jar -u yirupan -rp 8006 -rh localhost -s ${secret} > log/yirupan2.log 2>&1 &
