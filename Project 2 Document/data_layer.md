# Improvement For DS Project 2

## A data layer built across all servers should be designed

DataLayer Handlers

- Announce
- LockRequest/LockAllow/LockDenied
- UserInfo Update

```json
{
    "command":"USER_UPDATE",
    "username":"username",
    "secret":"secret",
    "loginTime":"loginTime",
    "logoutTime":"logoutTime",
    "udpateTime":"updateTime"
    ]
}
```

- UserInfo Sync

```json
{
    "command":"USER_SYNC",
    "user_list":[
        {"username":"username","secret":"secret","loginTime":"loginTime"...,
         "udpateTime":"updateTime"},
        {"username":"username","secret":"secret","loginTime":"loginTime"..., 					 "udpateTime":"updateTime"}
    ]
}
```
- activity update

```json
{
    "command":"ACTIVITY_UPDATE",
    "owner":"username",
    "activity_list":[
        {"activity":}
    ]
}
```

- activity sync

```json
{
    "command":"ACTIVITY_SYNC",
    "activity_entity":[
        {   "owner":"owner",
            "activity_list":[
            	{"activity":}
        	]
        }
	]
}
```





NetworkLayer Handlers

- Backup List
- Invalid Msg





Application

- Login

```json
{
    "command":"LOGIN",
    "username":"username",
    "secret":"secret"
}
```

- new server joins

```json
{
    "command":"AUTHETICSTE_SUCC",
    "user_list":[
        {"username":"username","secret":"secret","loginTime":"loginTime"...,
         "udpateTime":"updateTime"},
        {"username":"username","secret":"secret","loginTime":"loginTime"..., 					 "udpateTime":"updateTime"}
    ]
}
```

