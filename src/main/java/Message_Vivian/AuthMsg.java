package Message_Vivian;

public class AuthMsg  extends JsonMessage{
    private String secret = "";

    public AuthMsg()
    {
        setCommand(JsonMessage.AUTHENTICATE);
    }

    public void setSecret(String s)
    {
        secret = s;
    }
}
