package client.gui;

import client.Authorizable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JDialog {

    private boolean isAuthorized = false;
    private JTextField loginTextField;
    private JTextField passwordTextField;
    private JButton submitButton;
    private JLabel authLabel;


    private Authorizable authorizable;

    public LoginFrame(Authorizable authorizable, String login, String password){
        /* Класс, реализующий проверку авторизации. Приходит снаружи, чтобы не
         вносить во фрейм взаимодействие с сервером*/
        this.authorizable = authorizable;

        //Устанавливаем диалог модальным, чтобы остановить выполнение потока
        setModal(true);
        initComponents();

        loginTextField.setText(login);
        passwordTextField.setText(password);

        setVisible(true);
        submitButton.requestFocus();
    }

    private void initComponents(){
        setBounds(800, 500, 350, 150);

        //При закрытии диалога - прячем его
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        setResizable(false);

        setTitle("Введите логин и пароль для подключения");

        JPanel mainPanel = new JPanel(new GridLayout(4,1));
        authLabel = new JLabel("Введите логин и пароль");
        loginTextField = new JTextField();
        passwordTextField = new JTextField();
        submitButton = new JButton("Войти");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonOkClicked();
            }
        });

        mainPanel.add(authLabel);
        mainPanel.add(loginTextField);
        mainPanel.add(passwordTextField);
        mainPanel.add(submitButton);
        add(mainPanel);
    }

    private void buttonOkClicked(){
        //Проверку авторизации осуществляет объект, который мы передали в конструкторе
        isAuthorized = authorizable.makeAuthorization(loginTextField.getText(), passwordTextField.getText());
        if (isAuthorized) {
            //Для модального окна позволяет продолжить поток, в котором оно запущено
            setVisible(false);
        } else {
            authLabel.setText("Введен некорректный логин или пароль!");
        }

    }

    public boolean isAuthorized() {
        return isAuthorized;
    }
}