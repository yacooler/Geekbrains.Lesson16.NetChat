package server;

public class ChatMessage {
    public static final int CONT_SERVER_MESSAGE = 0;
    public static final int CONT_MESSAGE = 1;
    public static final int CONT_WISP_MESSAGE = 2;
    public static final int CONT_RENAME = 3;
    public static final int CONT_RENAME_DONE = 4;
    public static final int CONT_COMMAND = 5;
    public static final int CONT_ERROR = 6;
    public static final int CONT_END = 7;
    public static final int CONT_AUTH = 8;
    public static final int CONT_AUTH_DONE = 9;

    public static final String MESSAGE_AUTH = "/auth";
    public static final String MESSAGE_AUTH_DONE = "/authok";
    public static final String MESSAGE_WISP = "/w";
    public static final String MESSAGE_RENAME = "/rename";
    public static final String MESSAGE_SESSIONEND = "/end";

    private int content = -1;
    private String sender = "";
    private String recipient = "";
    private String message = "";



    /**
     * Сообщение инициализируемое строкой. Может содержать один или три блока, разделенные табуляцией
     * @param combinedMessage Сообщение, полученное с сервера
     */
    public ChatMessage(String combinedMessage){
        String[] msg = combinedMessage.split("\t");

        if (msg.length == 4) {
            content = Integer.valueOf(msg[0]);
            sender = msg[1];
            recipient = msg[2];
            message = msg[3];
        } else {
            message = combinedMessage.replace('\t', ' ');
        }
    }

    /**
     * Контент выделяется из контекста (1 слово сообщения может быть командой)
     * @param sender
     * @param message
     */
    public ChatMessage(String sender, String message){
        String firstWord = getFirstWord(message);
        this.sender = sender;

        switch (firstWord){
            case MESSAGE_WISP:{
                this.content = CONT_WISP_MESSAGE;
                this.recipient = getSecondWord(message);
                if (this.recipient.isEmpty()) return;
                this.message = getThirdTillEndWord(message);
                break;
            }
            case MESSAGE_RENAME:{
                this.content = CONT_RENAME;
                this.message = getSecondWord(message);
                break;
            }
            case MESSAGE_AUTH:{
                this.content = CONT_AUTH;
                break;
            }
            case MESSAGE_AUTH_DONE:{
                this.content = CONT_AUTH_DONE;
                break;
            }
            case MESSAGE_SESSIONEND:{
                this.content = CONT_END;
            }
            default: {
                this.content = CONT_MESSAGE;
                this.message = message;
            }
        }
    }

    /**
     * Системные сообщения - только контент и сообщение
     */
    public ChatMessage(int content, String message) {
        this.content = content;
        this.message = message;
        if (content == CONT_ERROR) {
            this.sender = "Error";
        } else {
            this.sender = "System";
        }
    }


    public String getSender() {
        //лдбддщдщдщщд это сделала кошка
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public int getContent(){
        return content;
    }

    public boolean isBlank(){
        return message.isBlank();
    }

    public String buildToSend(){
        return String.format("%d\t%s\t%s\t%s", content, sender, recipient, message);
    }


    private static String getFirstWord(String str){
        int index1 = str.indexOf(' ');
        if (index1 > 0){
            return str.substring(0, index1);
        }
        return str;
    }

    private static String getSecondWord(String str){
        int index1 = str.indexOf(' ');
        if (index1 < 0) return "";

        int index2 = str.indexOf(' ', index1 + 1);
        if (index2 < 0) return str.substring(index1 + 1);
        return str.substring(index1 + 1, index2);
    }

    private static String getThirdTillEndWord(String str){

        String firstWord = getFirstWord(str);
        if (firstWord.isEmpty()) return "";

        String secondWord = getSecondWord(str);
        if (secondWord.isEmpty()) return "";

        int index = firstWord.length() + secondWord.length() + 2;
        if (index >= str.length()) return "";

        return str.substring(index);
    }

}
