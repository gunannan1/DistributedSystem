
### High Available & Consistancy
#### general
1. MessageGenerator for BACKUP_LIST(done)

#### Client:
1. Backup message handler (done)
2. re-connect to backup server (pending)
3. show backup servers info on UI (done)


#### Server:
1. backup message handler for connection
2. re-connect to backup server
3. show backup servers info on UI

### Message Order Ensure
1. Update `ACTIVITY_MESSAGE` protocol. 
1. Buffer message as server side when sending to client (only to client need buffer)
2. Sort buffered messages when send to clients(only to client need sorting)\

### Message Delivery Ensure
1. Update `ACTIVITY_MESSAGE` protocol. 
1. Buffer message as server side when sending to client (only to client need buffer)
2. Sort buffered messages when send to clients(only to client need sorting)

### Message Complexity


### 