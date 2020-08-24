package server;

public class ChatMessage {
    private String sender = "";
    private String recipient = "";
    private String message = "";

    /**
     * Сообщение инициализируемое строкой. Может содержать один или три блока, разделенные табуляцией
     * @param combinedMessage Сообщение, полученное с сервера
     */
    public ChatMessage(String combinedMessage){
        String[] msg = combinedMessage.split("\t");

        if (msg.length == 3) {
            sender = msg[0];
            recipient = msg[1];
            message = msg[2];
        } else {
            message = combinedMessage.replace('\t', ' ');
        }
    }


    /**
     * Создание сообщения с клиента
     * @param sender Отправитель
     * @param message Сообщение с возможным получателем
     */
    public ChatMessage(String sender, String message) {
        message = message.replace('\t', ' ');
        this.sender = sender;
        //Если это приватное сообщение - выделим получателя и само сообщение
        if (message.startsWith(Server.WHISP_MESSAGE)){
            if (message.length() > Server.WHISP_MESSAGE.length()){

                //Убедимся, что указан получатель
                if (message.indexOf(' ', Server.WHISP_MESSAGE.length()) < 0){
                    return;
                }

                //Выделили получателя
                this.recipient = message.substring(Server.WHISP_MESSAGE.length(), message.indexOf(' ', Server.WHISP_MESSAGE.length()));

                //Выделили сообщение
                this.message = message.substring(Server.WHISP_MESSAGE.length() + recipient.length());

            }
        } else {
            this.message = message;
        }
        this.message = this.message.trim();
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

    public boolean isEndMessage(){
       return  (message.equals(Server.END_MESSAGE));
    }

    public boolean isPrivateMessage(){
        return !recipient.isBlank();
    }

    public boolean isBlank(){
        return message.isBlank();
    }

    public String buildToSend(){
        return String.format("%s\t%s\t%s", sender, recipient, message);
    }

}
