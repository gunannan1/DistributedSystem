# TODO List Summary


## Client 
##### Client.java
0. add new option to tell command type(connect or register)
0. add exception handle for lack of arguments.

##### ClientSkeleton.java
0. *Socket should be initialised and maintained here
0. rediction
0. register 
0. authorise
0. Some method can be moved to other class.

##### TextFrame

0. Message receiving need to run in another thread or it will block the UI thread
0. \* Message parse & print should also be in the receiving thread
0. Close all threads when disconnect.

##### *ReceiveThread.java
0. New class designed for receiving message
0. Parse incoming message and print to screen(in output area/panel)


## Server



 