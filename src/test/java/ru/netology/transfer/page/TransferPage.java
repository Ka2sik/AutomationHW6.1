package ru.netology.transfer.page;

import com.codeborne.selenide.SelenideElement;
import ru.netology.transfer.data.DataHelper;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class TransferPage {
    private final SelenideElement transferHeader = $(byText("Пополнение карты"));
    private final SelenideElement transferAmountField = $("[data-test-id=amount] input");
    private final SelenideElement transferCardFrom = $("[data-test-id=from] input");
    private final SelenideElement transferButton = $("[data-test-id=action-transfer]");
    private final SelenideElement errorNotification = $("[data-test-id=error-notification] .notification__content");
    private final SelenideElement cancelButton = $("[data-test-id=action-cancel]");

    public TransferPage() {
        transferHeader.shouldBe(visible);
    }

    public void transferMoney(String transferAmount, DataHelper.CardInfo cardInfo) {
        transferAmountField.setValue(transferAmount);
        transferCardFrom.setValue(cardInfo.getCardNumber());
        transferButton.click();
    }

    public DashboardPage validTransfer(String transferAmount, DataHelper.CardInfo cardInfo) {
        transferMoney(transferAmount, cardInfo);
        return new DashboardPage();
    }

    public void findErrorNotification(String expectedText) {
        errorNotification.shouldBe(visible).shouldHave(text(expectedText), Duration.ofSeconds(15));
    }

    public void cancelTransfer() {
        cancelButton.click();
    }
}
