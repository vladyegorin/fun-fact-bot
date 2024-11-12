package tutorial;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

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
            sendText(id, "Cool sticker!");
        }
        else if (msg.hasPhoto()) {
            System.out.println("Photo received");
            sendText(id, "Cool photo!");
        }
        else if (msg.hasVoice()) {
            System.out.println("Voice received");
            sendText(id, "You have a beautiful voice!");
        }
        else if (msg.hasAudio()) {
            System.out.println("Audio received");
            sendText(id, "Cool song!");
        }
        else if (msg.hasVideoNote()) {
            System.out.println("Kruzhok received");
            sendText(id, "You've got a pretty face!");
        }
        else if (msg.hasDocument()) {
            System.out.println("Document received");
            sendText(id, "Unfortunately, I cannot read a document.");
        }
        else if (msg.hasVideo()) {
            System.out.println("Video received");
            sendText(id, "Cool video!");
        }
        else if (msg.hasAnimation()) {
            System.out.println("GIF received");
            sendText(id, "Nice GIF!");
        }
        else {
            System.out.println("Unknown message type");
            sendText(id,"Unfortunately, I do not understand what you sent me :(");
        }

        if(msg.hasText() & msg.getText().equals("/fact")) {
            //fact
            String funfact = getRandomFunFact();
            sendText(id, "Sure! Here is a fun fact for you :\n" + funfact);


        }
        else if(msg.hasText() & msg.getText().equals("/help")) {
            sendText(id,"Welcome to Fun Fact Bot!\nYou can ask me for a fun fact by typing      /fact.\n" +
                    "If you don't want a fun fact, you can just try talking to me. Send me some pictures, kruzhochki, audio messages etc.");
        }
        else {
            sendText(id, "I do not quite understand. Try sending another message.");
        }



    }
    private String getRandomFunFact(){
        String funFact = "No fun fact available.";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:facts.sqlite");
             Statement stmt = conn.createStatement()) {

            String sql = "SELECT fact FROM fun_facts ORDER BY RANDOM() LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                funFact = rs.getString("fact");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return funFact;
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