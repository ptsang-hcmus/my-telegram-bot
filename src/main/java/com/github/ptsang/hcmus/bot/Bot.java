package com.github.ptsang.hcmus.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.market.TickerStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

@Component
public class Bot extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(Bot.class);

	private static List<String> followCoins = Arrays
			.asList(new String[] { "BTC", "ETH", "DOT", "ADA", "LINK", "LTC", "SFP" });

	Bot() {
		followCoins.sort((final String a, final String b) -> a.compareTo(b));
	}

	@Value("${bot.token}")
	private String token;

	@Value("${bot.username}")
	private String username;

	@Value("${bot.my_chatid}")
	private Long myChatId;

	@Value("${binance.api_key}")
	private String binanceApiKey;

	@Value("${binance.secret_key}")
	private String binanceSecretKey;

	public Long getMyChatId() {
		return myChatId;
	}

	@Override
	public String getBotToken() {
		return token;
	}

	@Override
	public String getBotUsername() {
		return username;
	}

	void sendAMessageToMyBot(String userChatText) {
		SendMessage response = new SendMessage();
		response.setChatId(myChatId);
		response.setText(userChatText);
		response.setParseMode("html");
		try {
			execute(response);
		} catch (TelegramApiException e) {
			logger.error("Failed to send message \"{}\" to {} due to error: {}", userChatText, myChatId,
					e.getMessage());
		}
	}

	void priceHandler() {
		SendMessage response = new SendMessage();
		response.setChatId(myChatId);
		String text = "Choosing 1 from the list of your followed coins";
		response.setText(text);
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		List<String> followCoins = Arrays.asList(new String[] { "BTC", "DOGE", "ETH", "DOT", "ADA", "LINK" });
		followCoins.sort((final String a, final String b) -> a.compareTo(b));
		for (String followCoin : followCoins) {
			rowInline.add(new InlineKeyboardButton().setText(followCoin).setCallbackData(followCoin));
			if (rowInline.size() == 2) {
				rowsInline.add(rowInline);
				rowInline = new ArrayList<InlineKeyboardButton>();
			}
		}
		if (!rowInline.isEmpty()) {
			rowsInline.add(rowInline);
		}
		rowsInline.add(rowInline);
		markupInline.setKeyboard(rowsInline);
		response.setReplyMarkup(markupInline);
		try {
			execute(response);
		} catch (TelegramApiException e) {
			logger.error("Failed to send message \"{}\" to {} due to error: {}", text, myChatId, e.getMessage());
		}
	}

	void accountHandler() {
		try {
			BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(binanceApiKey, binanceSecretKey);
			BinanceApiRestClient client = factory.newRestClient();

			long serverTime = client.getServerTime();
			Account account = client.getAccount(1000L, serverTime);

			List<BinanceAsset> balances = new ArrayList<BinanceAsset>();
			double totalUSDTAccount = 0;

			TickerStatistics tickerStatistics = client.get24HrPriceStatistics("BTCUSDT");
			double btcPrice = Double.parseDouble(tickerStatistics.getLastPrice());

			for (AssetBalance balance : account.getBalances()) {
				double free = Double.parseDouble(balance.getFree());
				if (free == 0) {
					continue;
				}

				double totalUSDTOfThisAsset = 0;

				if ("USDT".equals(balance.getAsset())) {
					totalUSDTOfThisAsset = free;
				} else {
					try {
						tickerStatistics = client.get24HrPriceStatistics(balance.getAsset() + "USDT");
						totalUSDTOfThisAsset = free * Double.parseDouble(tickerStatistics.getLastPrice());
					} catch (Exception e) {
						try {
							tickerStatistics = client.get24HrPriceStatistics(balance.getAsset() + "BTC");
							double totalBTCOfThisAsset = free * Double.parseDouble(tickerStatistics.getLastPrice());
							totalUSDTOfThisAsset = totalBTCOfThisAsset * btcPrice;
						} catch (Exception ee) {
							logger.error(balance.getAsset());
							sendAMessageToMyBot(ee.toString());
						}
					}
				}

				BinanceAsset bs = new BinanceAsset();
				bs.setAsset(balance.getAsset());
				bs.setFree(Double.parseDouble(balance.getFree()));
				bs.setWorth(totalUSDTOfThisAsset);
				totalUSDTAccount += totalUSDTOfThisAsset;

				balances.add(bs);
			}
			Collections.sort(balances);
			StringBuilder sb = new StringBuilder();
			sb.append("<i>Total USDT in account: </i> <b>" + String.format("%.3f", totalUSDTAccount) + "</b>\n\n");
			sb.append("<i>Asset list:</i>\n");
			for (BinanceAsset balance : balances) {
				sb.append("<i>" + balance.toString() + "</i>\n");
			}
			sendAMessageToMyBot(sb.toString());
		} catch (

		Exception e) {
			logger.error("Error: {}", e);
			sendAMessageToMyBot(e.toString());
		}
	}

	void myGirlHandler() {
		try {
			sendAMessageToMyBot("<b>Nguyễn Đức Quỳnh Như</b> - <i>19/05/1999</i>");
		} catch (Exception e) {
			logger.error("Error: {}", e);
		}
	}

	void snipsHandler() {
		SendMessage response = new SendMessage();
		response.setChatId(myChatId);
		String text = "What do you want?";
		response.setText(text);
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

		List<String> queries = Arrays
				.asList(new String[] { "LOWER_BOUND", "UPPER_BOUND", "TRIE", "BINARY_SEARCH", "QUICK_SORT" });
		queries.sort((final String a, final String b) -> a.compareTo(b));
		for (String query : queries) {
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
			rowInline.add(new InlineKeyboardButton().setText(query).setCallbackData(query));
			rowsInline.add(rowInline);
		}
		// Set the keyboard to the markup

		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		response.setReplyMarkup(markupInline);
		try {
			execute(response);
		} catch (TelegramApiException e) {
			logger.error("Failed to send message \"{}\" to {} due to error: {}", text, myChatId, e.getMessage());
		}
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage()) {
			Message message = update.getMessage();
			Long chatId = message.getChatId();
			if (!myChatId.equals(chatId)) {
				return;
			}

			String text = message.getText();
			switch (text) {
			case "/price":
				priceHandler();
				break;
			case "/account":
				accountHandler();
				break;
			case "/my_girl":
				myGirlHandler();
				break;
			case "/snips":
				snipsHandler();
				break;
			default:
				sendAMessageToMyBot(text);
			}
		} else if (update.hasCallbackQuery()) {
			// Set variables
			String call_data = update.getCallbackQuery().getData();
			long message_id = update.getCallbackQuery().getMessage().getMessageId();
			long chat_id = update.getCallbackQuery().getMessage().getChatId();
			if (followCoins.contains(call_data)) {
				StringBuilder sb = new StringBuilder();
				sb.append("Price of <b>__" + call_data + "__</b> at <i>" + (new Date()).toString() + "</i>");
				BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(binanceApiKey, binanceSecretKey);
				BinanceApiRestClient client = factory.newRestClient();
				TickerStatistics tickerStatistics = client.get24HrPriceStatistics(call_data + "USDT");
				sb.append("\n<i>Symbol:</i> " + tickerStatistics.getSymbol());
				sb.append("\n<b>PriceChange:</b> " + tickerStatistics.getPriceChange());
				sb.append("\n<b>PriceChangePercent:</b> " + tickerStatistics.getPriceChangePercent());
				sb.append("\n<b>LowPrice:</b> " + tickerStatistics.getLowPrice());
				sb.append("\n<b>HighPrice:</b> " + tickerStatistics.getHighPrice());
				Double lo = Double.parseDouble(tickerStatistics.getLowPrice()),
						hi = Double.parseDouble(tickerStatistics.getHighPrice());
				sb.append("\n<b>LowHighPriceChange:</b> " + String.format("%f", (hi - lo)));
				sb.append("\n<b>LowHighPriceChangePercent:</b> " + String.format("%f", ((hi - lo) / lo * 100)));
				sb.append("\n<b>LastPrice:</b> " + tickerStatistics.getLastPrice());

				EditMessageText new_message = new EditMessageText().setParseMode("html").setChatId(chat_id)
						.setMessageId((int) message_id).setText(sb.toString());
				try {
					execute(new_message);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}
			} else {
				EditMessageText new_message = new EditMessageText().setParseMode("html").setChatId(chat_id)
						.setMessageId((int) message_id).setText("UNDEFINED answer for: <b>" + call_data + "</b>");
				try {
					execute(new_message);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@PostConstruct
	public void start() {
		logger.info("username: {}, token: {}", username, token);
	}

}
