package tutorial;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class Bot extends TelegramLongPollingBot {
    private String botToken;
    private String state;
    private InlineKeyboardButton animal = InlineKeyboardButton.builder().text("Animal Fun Fact").callbackData("animal").build();
    private InlineKeyboardButton human = InlineKeyboardButton.builder().text("Human Fun Fact").callbackData("human").build();
    private InlineKeyboardButton plants = InlineKeyboardButton.builder().text("Plant Fun Fact").callbackData("plant").build();
    private InlineKeyboardButton randomFF = InlineKeyboardButton.builder().text("Random Fun Fact").callbackData("random").build();
    private InlineKeyboardMarkup keyboardFactType = InlineKeyboardMarkup.builder().keyboardRow(List.of(animal)).keyboardRow(List.of(human)).keyboardRow(List.of(plants)).keyboardRow(List.of(randomFF)).build();


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
            sendText(id, "Nice sticker!");
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
            sendText(id, "Cool song! You rock!");
        }
        else if (msg.hasVideoNote()) {
            System.out.println("Kruzhok received");
            sendText(id, "You've got a pretty face!");
        }
        else if (msg.hasDocument()) {
            System.out.println("Document received");
            sendText(id, "Unfortunately, I cannot read a document, but I bet there is something cool in it.");
        }
        else if (msg.hasVideo()) {
            System.out.println("Video received");
            sendText(id, "Amazing video!");
        }
        else if (msg.hasAnimation()) {
            System.out.println("GIF received");
            sendText(id, "Nice GIF!");
        }
        else {
            System.out.println("Unknown message type");
            sendText(id,"Unfortunately, I do not understand what you sent me :(");
        }
        if(msg.hasText()) {
            if (msg.getText().equals("/fact")) {
                //fact
                sendMenu(id, "<b>Which type of a fun fact would you like to get?</b>", keyboardFactType);
                //see official tg guide
                //1)remake the db,delete all old facts, add "type" column, populate with new facts with giving them a type
                //2)remake getRandomFunFact function. make a variable that will take the "type" of the fact
                //3)if type = random, give ANY fact(this means original getRandomFunFact would work)
                //String funfact = getRandomFunFact();
                //sendText(id, "Sure! Here is a fun fact for you :\n" + funfact);


            } else if (msg.getText().equals("/start")) {
                sendText(id, "Welcome to Fun Fact Bot!\nAsk me for a fun fact by typing /fact\n" +
                        "If you don't want a fun fact, send me some pictures, kruzhochki, voice messages, stickers etc.");

            } else if (msg.getText().equals("/help")) {
                sendText(id, "Use /fact and click on one of the buttons to choose which type of a fun fact you would like to get 😊");
            } else if (msg.getText().equals("Hello") || msg.getText().equals("Hi") || msg.getText().equals("hello") || msg.getText().equals("hi")) {
                sendText(id, "Hi there!");
            } else if (msg.getText().equals("Привет") ||msg.getText().equals("привет")){
                sendText(id,"Приветики!");
            } else{
                sendText(id, "I don't really understand. Try sending another type of message or a command.");
            }
        }



    }
    public void sendMenu(Long who, String txt, InlineKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(kb).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private String getRandomFunFact() {
        String funFact = "No fun fact available.";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:facts.sqlite");
             Statement stmt = conn.createStatement()) {


            String countQuery = "SELECT COUNT(*) AS count FROM fun_facts WHERE shown = 0";
            ResultSet countRs = stmt.executeQuery(countQuery);
            int unshownCount = 0;
            if (countRs.next()) {
                unshownCount = countRs.getInt("count");
            }

            // If all facts have been shown, reset the `shown` column
            if (unshownCount == 0) {
                String resetQuery = "UPDATE fun_facts SET shown = 0";
                stmt.executeUpdate(resetQuery);
            }

            // Select a random unshown fact
            String sql = "SELECT fact FROM fun_facts WHERE shown = 0 ORDER BY RANDOM() LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                funFact = rs.getString("fact");

                // Mark the fact as shown
                String updateQuery = "UPDATE fun_facts SET shown = 1 WHERE fact = ?";
                try (var pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setString(1, funFact);
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return funFact;
    }
    //private void userAddFunFact(String fact){

    //}

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