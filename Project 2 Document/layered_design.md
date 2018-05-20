# Improvement For DS Project 2

Check detail instructure of project 1 [here](../Readme.md)

### Improvement instructure

A three layered structure is applied in this project

#### Application Layer

This layer conducts normal functionalities of this application.

| Message             | Description                               | Action                                                       |
| ------------------- | ----------------------------------------- | ------------------------------------------------------------ |
| ActivityRequest     | Activity message from Clients             | Add new activity into data layer.<br />``` DataLayer.getInstance().insertActivity(activity,connection);``` |
| ServerAuthenRequest | Authen message from new server            | Check secret and response accordingly.                       |
| ServerAuthenFailed  | Failed auth result from connecting server | close connection and exit.                                   |
| ServerAuthenSucc    | Succ auth result from connecting server   | Begin to provide service                                     |
| UserLogin           | Login message from clients                | Query user info from data layer and response accordingly<br />```DataLayer.getInstance().getUserByName(username);```<br />```DataLayer.getInstance().markUserOnline(username, true);``` |
| UserLogout          | Logout message from clients               | Mark user as "logout" in data layer.<br />```DataLayer.getInstance().markUserOnline(username,false);``` |
| UserRegisterHandler | Register message from clients             | Register user in data layer and response accordingly.<br />```DataLayer.getInstance().registerUser(username,secret,connection);```<br />1. If user exists, reply REGISTER_FAILED<br />2. else, run register process same as project 1 in data layer |
| RegisterResult      | Result fom data layer                     | When register process finishes, data layer will send this message to application layer. Application Layer reply to that particular client with this register result. |



#### DataLayer

This layer is in charge of **creating/updating/deleting** data and **sync data** with other servers' data layer.

| Message                        | Description                                                  | Action                                                       |
| ------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| LockRequest                    | Lock every server for a partifular username                  | When `registerUser()` is called and user does not in data layer, data layer will boradcast this message to all servers to lock register process for this particular username |
| LockAllowed                    | A server successes to lock a username                        | When a server successfully locks a given username for register process, reply this message. |
| LockDenied                     | A server fails to lock a username                            | When a server fails to lock a given username for register process(already exists in its register process), reply this message. |
| BroadcastResult(not a message) | When all lock replies are received, use this for final result. | Check whether all replies are LockAllowed or at least one LockDenied:<br />\|- if all allowed: reply lockallowed(If lockrequest from server) or delegate `RegisterSucc` to application layer(if register request from clients)<br />|- if at least one denied: reply LockDenied(If lockrequest from server) or delegate `RegisterFailed` to application layer(if register request from clients) |
| ActivityBroadcast              | New activity from other servers                              | Add activity into its own data layer and broadcast to other servers(excpet the sending one) |
| ServerAnnounce                 | Server status from other servers                             | Update server status in its own data layer                   |
| UserSync                       | All user info to sync                                        | Compare all users' info with local data layer and update accrodingly<br />**compare `updateTime` first and decide whether need updating** |
| UserUpdate                     | A particular user info                                       | Compare a particular user info with local data layer and create/update accordingly.<br />**compare `updateTime` first and decide whether need updating** |
| ActivitySync                   | Activity lists for all registered users to sync              | Compare with local data layer and create/update accordingly.<br />**compare `updateTime` of activity first and decide whether need updating** |
| ActivityUpdate                 | A particular user's activity list                            | Compare with local data layer and create/update accordingly.<br />**compare `updateTime` of activity first and decide whether need updating** |

#### Network Layer

This laye is responsible for maintaining connections, sending/receiving data and delivering different types of message to different data consumer.

| Message                 | Description                   | Action                                                       |
| ----------------------- | ----------------------------- | ------------------------------------------------------------ |
| ServerBackupListHandler | A server's backup information | Update related connection's backup info, which is used when that connection is brokn. |
| ServerInvalidHandler    | In validate message           | If a message without `command` or no consumer can handle the `command` of this message, reply `INVALID_MESSAGE`. |
|                         |                               |                                                              |



### How to set up development environment

```bash
git clone https://kangnwh.visualstudio.com/DistributedSystem/_git/DistributedSystem
```



#### New protocols 

- UserInfo Update

```json
{
    "command":"USER_UPDATE",
    "username":"username",
    "secret":"secret",
    "online":"true/false",
    "udpateTime":"updateTime"
}
```

- UserInfo Sync

```json
{
    "command":"USER_SYNC",
    "user_list":[
        {"username":"username","secret":"secret","online":"true/false",
         	"udpateTime":"updateTime"},
        {"username":"username","secret":"secret","online":"true/false",					 			"udpateTime":"updateTime"}
        ...
    ]
}
```
- activity update

```json
{
    "command":"ACTIVITY_UPDATE",
    "owner":"username",
    "activity_list":[
        {
         "udpateTime":"updateTime",
         "sendTime":"sendTime",
         "isDelivered":"false/ture",
         "activity":{"authenticated_user":"authenticated_user","other":"other"}
        },
        { 
         "udpateTime":"updateTime",
         "sendTime":"sendTime",
         "isDelivered":"false/ture",
         "activity":{"authenticated_user":"authenticated_user","other":"other"}
        }
        ...
    ]
}
```

- activity sync

```json
{
    "command":"ACTIVITY_SYNC",
    "activity_entity":[
        {USER1_ACTIVITY_UPDATE_JSON},
        {USER2_ACTIVITY_UPDATE_JSON},
        ...
	]
}
```

- AUTHETICSTE_SUCC

```json
{
    "command":"AUTHETICSTE_SUCC",
    "user_list":USER_SYNC_JSON,
    "activity_entity":ACTIVITY_SYNC_JSON
}
```

