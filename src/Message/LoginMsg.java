package Message;


import com.oracle.javafx.jmx.json.impl.JSONMessages;

public class LoginMsg extends JsonMessage {
    private String username = "";
    private String secret = "";

    public LoginMsg()
    {
        setCommand(JsonMessage.LOGIN);
    }
    public void setUsername(String n){
        username = n;
    }
    public void setSecrect(String s)
    {
        secret = s;
    }
}
