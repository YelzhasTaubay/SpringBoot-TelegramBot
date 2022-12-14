package com.example.YelzhasDemoBot.service;

import com.example.YelzhasDemoBot.config.BotConfig;
import com.example.YelzhasDemoBot.model.UserRepository;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot{

    @Autowired
    private final UserRepository userRepository;
    final BotConfig config;

    static final String HELP_TEXT="This bot is created to demonstrate Spring capabilities.\n\n"+
            "You can execute commands from the main menu on the left or by typing a command:\n\n"+
            "Type /start to see a welcome message\n\n"+
            "Type /mydata to see data stored about yourself\n\n"+
            "Type /help to see this message again";

    public TelegramBot(BotConfig config, UserRepository userRepository){
        this.config=config;
        List<BotCommand> listofCommand=new ArrayList<>();
        listofCommand.add(new BotCommand("/start","get a welcome message"));
        listofCommand.add(new BotCommand("mydata","get your data stored"));
        listofCommand.add(new BotCommand("/deletedata","delete my data"));
        listofCommand.add(new BotCommand("/help","info how to use this bot"));
        listofCommand.add(new BotCommand("/settings","set your preferences"));
        try {
            this.execute(new SetMyCommands(listofCommand,new BotCommandScopeDefault(),null));
        }catch (TelegramApiException e){
            log.error("Error setting bots command list: "+e.getMessage());
        }
        this.userRepository = userRepository;
    }

    @Override
    public String getBotUsername(){
        return config.getBotName();
    }

    @Override
    public String getBotToken(){
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update){
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText=update.getMessage().getText();
            long chatId=update.getMessage().getChatId();

            switch (messageText){
                case "/start":

                    registerUser(update.getMessage());
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName());
                    break;

                case "/help":

                    sendMessage(chatId,HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId,"Sorry, command was not recognized");
            }

        }
    }

    private void registerUser(Message msg) {

        if(userRepository.findById(msg.getChatId()).isEmpty()){

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setId(chatId);    //???????????????? ?????? ????????????
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            //user.setregisteredAt(chat.)  // ?????? ????????????

            //userRepository.save(user); // ?????? ????????????
            log.info("user saved: " + user);
        }
    }

    private void startCommandReceived(long chatId,String name){

        String answer= EmojiParser.parseToUnicode("Hi, "+name+" nice to meet you!"+" :blush:");
        log.info("Replied to user "+name);

        sendMessage(chatId,answer);

    }

    private void sendMessage(long chatId,String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("weather");
        row.add("get random joke");

        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("register");
        row.add("check my data");
        row.add("delete my data");

        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " +e.getMessage());
        }
    }

}
