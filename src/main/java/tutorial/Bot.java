package tutorial;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Properties;

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
        if (update.hasMessage()) {
            var msg = update.getMessage();
            var user = msg.getFrom();
            var id = user.getId();

            System.out.println("\nNew message!");
            System.out.println("User ID: " + id);
            System.out.println("Username: " + user.getUserName());

            if (msg.hasText()) {
                System.out.println("Text message: " + msg.getText());
                handleTextMessages(msg, id); // Calls the function to handle text messages
            } else {
                handleMediaAndOtherMessages(msg, id); // Calls the function to handle media or other types of messages
            }
        }

        if (update.hasCallbackQuery()) {
            // Handle the button click from the inline keyboard
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        var userId = callbackQuery.getFrom().getId();
        var data = callbackQuery.getData();  // This is the callback data sent by button clicks

        System.out.println("Callback query received: " + data);

        // Fetch and send the fact based on the button clicked
        String funFact = getRandomFunFact(data);
        sendText(userId, "Here is your " + data + " fun fact: \n" + funFact);
    }

    private void handleTextMessages(Message msg, Long id){
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
        }
        else if (msg.getText().equals("Thank you")){
            sendText(id,"You are welcome!");
        }
        else{
            sendText(id, "I don't really understand. Try sending another type of message or a command.");
        }
    }

    private void handleMediaAndOtherMessages(Message msg, Long id) {
        if (msg.hasSticker()) {
            System.out.println("Sticker received");
            sendText(id, "Cool sticker!");
        } else if (msg.hasPhoto()) {
            System.out.println("Photo received");
            sendText(id, "Cool photo!");
        } else if (msg.hasVoice()) {
            System.out.println("Voice received");
            sendText(id, "You have a beautiful voice!");
        } else if (msg.hasAudio()) {
            System.out.println("Audio received");
            sendText(id, "Cool song!");
        } else if (msg.hasVideoNote()) {
            System.out.println("Kruzhok received");
            sendText(id, "You've got a pretty face!");
        } else if (msg.hasDocument()) {
            System.out.println("Document received");
            sendText(id, "Unfortunately, I cannot read a document.");
        } else if (msg.hasVideo()) {
            System.out.println("Video received");
            sendText(id, "Cool video!");
        } else if (msg.hasAnimation()) {
            System.out.println("GIF received");
            sendText(id, "Nice GIF!");
        } else {
            System.out.println("Unknown message type");
            sendText(id, "Unfortunately, I do not understand what you sent me :(");
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

    private String getRandomFunFact(String factType) {
        String funFact = "No fun fact available.";
        String sql;

        if ("random".equals(factType)) {
            //choose random unshown fact
            sql = "SELECT fact FROM fun_facts WHERE shown = 0 ORDER BY RANDOM() LIMIT 1";
        } else {
            //choose specific unshown fact with a specific fact_type
            sql = "SELECT fact FROM fun_facts WHERE fact_type = ? AND shown = 0 ORDER BY RANDOM() LIMIT 1";
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:facts.sqlite");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (!"random".equals(factType)) {
                pstmt.setString(1, factType);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                funFact = rs.getString("fact");


                String updateSql = "UPDATE fun_facts SET shown = 1 WHERE fact = ?"; //mark fact as shown
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                    updatePstmt.setString(1, funFact);
                    updatePstmt.executeUpdate();
                }
            } else {
                // if all facts of a certain type got shown, reset the shown column
                String resetSql;
                if ("random".equals(factType)) {
                    resetSql = "UPDATE fun_facts SET shown = 0";  // reset all facts if random is selected
                } else {
                    resetSql = "UPDATE fun_facts SET shown = 0 WHERE fact_type = ?";  // reset shown for specific type
                }

                try (PreparedStatement resetPstmt = conn.prepareStatement(resetSql)) {
                    if (!"random".equals(factType)) {
                        resetPstmt.setString(1, factType);
                    }
                    resetPstmt.executeUpdate();
                }

                // Retry fetching the fact after reset
                return getRandomFunFact(factType);
            }

        } catch (SQLException e) {
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