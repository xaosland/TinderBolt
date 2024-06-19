package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "TinderRushBot";
    public static final String TELEGRAM_BOT_TOKEN = "";
    public static final String OPEN_AI_TOKEN = "";
    private ChatGPTService chatGPTService = new ChatGPTService(OPEN_AI_TOKEN);

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();
        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("главное меню бота 🎪", "/start",
                    "генерация Tinder-профля 🤳", "/profile",
                    "сообщение для знакомства 🧧, ", "/opener",
                    "переписка от вашего имени  🧨, ", "/message",
                    "переписка со звездами 🔥", "/date",
                    "задать вопрос чату GPT 👻", "/gpt");
            return;
        }
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }
        if (currentMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");
            Message msg = sendTextMessage("Подготовьтесь к свиданию! chatGpt думает...");
            String answer = chatGPTService.sendMessage(prompt, message);
            sendTextMessage(answer);
            return;
        }
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }
        if (currentMode == DialogMode.DATE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор! \n Твоя задача пригласить на свидание за 5 сообщений!");
                String prompt = loadPrompt(query);
                chatGPTService.setPrompt(prompt);
                return;
            }
            Message msg = sendTextMessage("Подготовьтесь к свиданию! chatGpt думает...");
            String answer = chatGPTService.addMessage(message);
            sendTextMessage(answer);
            return;
        }
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }
        if (currentMode == DialogMode.MESSAGE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);
                Message msg = sendTextMessage("Подготовьтесь к свиданию! chatGpt думает...");
                String answer = chatGPTService.sendMessage(prompt, userChatHistory);
                sendTextMessage(answer);
            }

            list.add(message);
            return;
        }

        sendTextMessage("Приветствую вас! 🥰");
        sendTextMessage("Вы готовы узнать сегодня что-то новое? ");
        sendTextMessage("Хотите узнать про " + message + " ?");
        sendTextButtonsMessage("Выберите режим работы:", "Старт", "/start", "Стоп", "/stop");

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}