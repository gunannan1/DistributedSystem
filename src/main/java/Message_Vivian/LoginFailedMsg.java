package Message_Vivian;

public class LoginFailedMsg extends JsonMessage{
    private String info;

    public LoginFailedMsg()
    {
        setCommand(JsonMessage.LOGIN_FAILED);
    }

    public void setInfo(String info)
    {
        this.info = info;
    }
}
