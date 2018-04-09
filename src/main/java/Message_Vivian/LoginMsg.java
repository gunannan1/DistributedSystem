package Message_Vivian;


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
