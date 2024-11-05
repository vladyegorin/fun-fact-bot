package tutorial;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Bot extends TelegramLongPollingBot {
    private String botToken;

    public Bot() {
        // Load properties from config file
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            botToken = properties.getProperty("TELEGRAM_BOT_TOKEN");
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error, maybe set a default or exit
        }
    }

    @Override
    public String getBotUsername() {
        return "SuperFunFactBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        System.out.println("New message!");
        //System.out.println(update);// -> prints all the info about the message
        System.out.println("User ID: " + id);
        System.out.println("Username: " + user.getUserName());

        if (msg.hasText()) {
            System.out.println("Text message: " + msg.getText());
        }
        else if (msg.hasSticker()) {
            System.out.println("Sticker received");
        }
        else if (msg.hasPhoto()) {
            System.out.println("Photo received");
        }
        else if (msg.hasVoice()) {
            System.out.println("Voice received");
        }
        else if (msg.hasAudio()) {
            System.out.println("Audio received");
        }
        else if (msg.hasVideoNote()) {
            System.out.println("Kruzhok received");
        }
        else if (msg.hasDocument()) {
            System.out.println("Document received");
        }
        else if (msg.hasVideo()) {
            System.out.println("Video received");
        }
        else if (msg.hasAnimation()) {
            System.out.println("GIF received");
        }
        else {
            System.out.println("Unknown message type");
        }

        if(msg.hasText()) {
            if (msg.getText().equals("Я тебя люблю")) {
                sendText(id, "И я тебя люблю бубуня");
            }
            else {
                copyMessage(id, msg.getMessageId());
            }
        }
        else{

            copyMessage(id, msg.getMessageId());
        }


    }
    public void copyMessage(Long who, Integer msgId){
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())  //We copy from the user
                .chatId(who.toString())      //And send it back to him
                .messageId(msgId)            //Specifying what message
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }


    //TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

}