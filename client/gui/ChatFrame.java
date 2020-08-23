package client.gui;

import client.MessageListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatFrame extends JFrame {

    JTextField chatField;
    JTextArea textArea;
    JButton submitButton;
    MessageListener messageListener;


    public ChatFrame(String title, MessageListener messageListener){
        initComponents(title);
        initListeners();
        setVisible(true);
        chatField.requestFocus();
        this.messageListener = messageListener;
    }

    public void pushMessage(String newMessage){
        if (!newMessage.isBlank()){
            textArea.append(newMessage + "\n");
        }
    }

    public void prepareMessage(String preparedMessage){
        if (!preparedMessage.isBlank()){
            chatField.setText(preparedMessage);
        }
    }

    private void initComponents(String title){
        setBounds(600, 300, 500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(title);
        JPanel mainPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        mainPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatPanel.add(chatField, BorderLayout.CENTER);
        submitButton = new JButton("Отправить");
        chatPanel.add(submitButton, BorderLayout.EAST);
        mainPanel.add(chatPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void initListeners(){
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        };

        submitButton.addActionListener(listener);
        chatField.addActionListener(listener);

    }

    private void sendMessage(){
        if (!chatField.getText().isBlank()){
            messageListener.messagePerformed(chatField.getText());
            chatField.setText("");
            chatField.requestFocus();
        }
    }



}
