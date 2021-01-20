package mirror42.dev.cinemates.mailAPI;

public class ConfigurationMailManager {

    private final static String HOST =  "smtp.gmail.com";
    private final static String PORT = "587" ;

    //email-Cinemate20-account
    private static String GMAIL_ID = "Cinemate20";
    private static String PASSWORD = "AA1999PIPPO";

    private ConfigurationMailManager(){

    }

    public static String getHost() {
        return HOST;
    }

    public static String getPort() {
        return PORT;
    }

    public static String getGmailId() {
        return GMAIL_ID;
    }

    public static String getPassword() {
        return PASSWORD;
    }


}
