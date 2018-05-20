# Improvement Design
### High Available 

1. [DONE]`BACKUP_LIST` Message to directly-connected `servers` AND `clients`. Every 5 seconds together with `BROADCAST_MESSAGE`
```json
{
    "command":"BACKUP_LIST",
    "servers":[
        {"host":"host_ip","port":"prot_num"},
        {"host":"host_ip2","port":"port_num2"}
    ]
}
```
2. [DONE]In order to get the `port` and `ip` of a server's directly-connected server, a server need to send its own `ip` and `service port to clients/servers` within its `AUTHEN` message when joining

```json
{
    "command":"AUTHENTICATE",
    "secret":"system_secret",
    "host":"ip to provide services(set from command line arg)",
    "port":"port to provide services(set from command line arg)"
}
```



2. [DONE]When server carshes, directly-connected `servers` AND `clients` will try to connect to `BACKUP_LIST` in order.
3. [DONE]New login and redirect process will be triggered when clients connect to backup servers. 
4. Whole system should be in a status of out-of-service if any server crash and be in a status of NORMAL if the reconnect action is done.
5. [DONE]If the load information of a server is not updated for 20 seconds, then mark this server as unavailable and remove it from `server load list`.

### Consistancy
1. [DONE]Servers connect to crashed server can automatically connect to a working server.
2. [DONE]clients connect to crashed server can automatically connect to a working server.


### Message Order Ensure
1. Every client has a variable `MESSAGE_SEQ` to indicate the sequence of the sending message is.
```json
{
    "command":"ACTIVITY_MESSAGE",
    "username":"username",
    "secret":"secret",
    "activity_seq":123,
    "activity":{}
}
```
2. When servers receive `activity`, they transfer this `activity` immediately but hold this message for a period of time(`PERIOD_FOR_ORDERING`) in case of out-of-order message issue.
3. If a messages with smaller `sequence number` comes in within the `PERIOD_FOR_ORDERING` time, all messages will be re-ordered and send to clients by their `sequence number`
4. **NOTE**: Servers only hold messages to their directly-connected **CLIENTS**, messages will be broadcasted immediately to other servers.

### Message Delivery Ensure

1. Every time when server sends an activity to its clients, it needs store how many sendings success and how many fail.
2. Server needs to reply this `success count` and `fail count` to the server/client where this activity comes from. So that we need
3. `ACTIVITY_SUCC_INFO` message sending from servers to source server:

```json
{
    "command":"ACTIVITY_SUCC_INFO",
    "username":"user who sends activity",
    "activity_seq":3,
    "client_count":12,
    "succ_count":10
}
```

4. When a intermedia receives all its 'children' servers ( for particular activity message), it calculates all succ/fail counts and reply to its 'parent(from)' server.
5. Finally when the server who receives `ACTIVITY_REQUEST` receives all its 'children' servers' reply, it calculates it and reply to client

```json
{
    "command":"ACTIVITY_SENDING_REPORT",
    "activity_seq":3,
    "client_count":29,
    "succ_count":28
}
```



### Message Complexity (Mainly about REGISTER process) [DONE]
1. When a user is registering, the server which receives `REGISTER` message will broadcast `LOCK_REQUEST` to the whole system. Instead of recording this username/secret pair into their **register_user list**, they store it into a **user_under_register list** for future use.

2. When the server receives enough `LOCK_ALLOWED` messages, it records this username/secret in its storage and broadcast another `NEW_USER_REGISTERED` message to the whole system, and all of servers will record this username/secret into their own storage.
```json
{
   "command":"USER_REGISTER_RESULT",
   "result":"succ/fail",
   "username":"username1",
   "secret":"secret1"
}
```
3. Servers receiving this message will remove the given username/secret from their `user_under_register` and add this user information into **regisetr_user list** or just ignore it.
4. If a new register request from clients, the server: 

- checks its local **register_user list**: if this username is in this list, just reply `REGISTER_FAILED` with info "username is registered"
- checks its own **user_under_register**: if this username is in this list, just reply `REGISTER_FAILED` with info "username is under regisetr process"
- if neither of above two happens, run from **step 1**.
- a **time-out** should be set and if time-out, send register fail

### New server joins[DONE]
1. When new server(Server A) joins, it sends "AUTHENTICATE" message to the connecting server(server B).
2. When Server B receives "AUTHENTICATE", it replys a "AUTHENTICATE_SUCC" message with **register_user list**:
```json
{
    "command":"AUTHETICSTE_SUCC",
    "
    "user_list":[
        {"username":"uername1","secret":"secret1"},
        {"username":"username2","secret":"secret2"},
        ...
    ]
}
```
