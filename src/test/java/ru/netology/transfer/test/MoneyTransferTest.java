package ru.netology.transfer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.transfer.data.DataHelper;
import ru.netology.transfer.page.DashboardPage;
import ru.netology.transfer.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.transfer.data.DataHelper.generateInvalidAmount;
import static ru.netology.transfer.data.DataHelper.generateValidAmount;

public class MoneyTransferTest {
    DataHelper.CardInfo firstCardInfo;
    DataHelper.CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;
    DashboardPage dashboardPage;

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
        var loginPage = new LoginPage();
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCode();
        dashboardPage = verificationPage.validVerification(verificationCode);
        firstCardInfo = DataHelper.getFirstCardInfo();
        secondCardInfo = DataHelper.getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(0);
        secondCardBalance = dashboardPage.getCardBalance(1);
        //var secondCardBalance = dashboardPage.getCardBalance(DataHelper.getMaskedNumber(secondCardInfo.getCardNumber()));
    }

    @Test
    void shouldTransferMoneyFromFirstCardToSecond() {
        var amount = generateValidAmount(firstCardBalance);
        var expectedFirstCardBalance = firstCardBalance - amount;
        var expectedSecondCardBalance = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        dashboardPage = transferPage.validTransfer(String.valueOf(amount), firstCardInfo);
        dashboardPage.reloadDashboardPage();
        int actualFirstCardBalance = dashboardPage.getCardBalance(0);
        int actualSecondCardBalance = dashboardPage.getCardBalance(1);
        assertAll(() -> assertEquals(expectedFirstCardBalance, actualFirstCardBalance),
                () -> assertEquals(expectedSecondCardBalance, actualSecondCardBalance));
    }

    @Test
    void shouldCancelTransferWithErrorIfAmountIsMoreThanBalance() {
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.transferMoney(String.valueOf(amount), secondCardInfo);
        assertAll(() -> transferPage.findErrorNotification("Сумма превода превышает остаток на карте списания"),
                transferPage::cancelTransfer,
                () -> dashboardPage.reloadDashboardPage(),
                () -> assertEquals(firstCardBalance, dashboardPage.getCardBalance(0)),
                () -> assertEquals(secondCardBalance, dashboardPage.getCardBalance(1)));
    }

    @Test
    void shouldCancelTransferWithErrorIfAmountIsEmpty() {

        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.transferMoney("", secondCardInfo);
        assertAll(() -> transferPage.findErrorNotification("Поле 'Сумма' не заполнено"),
                transferPage::cancelTransfer,
                () -> dashboardPage.reloadDashboardPage(),
                () -> assertEquals(firstCardBalance, dashboardPage.getCardBalance(0)),
                () -> assertEquals(secondCardBalance, dashboardPage.getCardBalance(1)));
    }

    @Test
    void shouldCancelTransferWithErrorIfAmountIsZero() {

        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        transferPage.transferMoney("0", firstCardInfo);
        assertAll(() -> transferPage.findErrorNotification("Сумма превода не может быть меньше 1"),
                transferPage::cancelTransfer,
                () -> dashboardPage.reloadDashboardPage(),
                () -> assertEquals(firstCardBalance, dashboardPage.getCardBalance(0)),
                () -> assertEquals(secondCardBalance, dashboardPage.getCardBalance(1)));
    }

    @Test
    void shouldCancelTransferWithErrorIfCardInfoIsEmpty() {
        DataHelper.CardInfo empty = new DataHelper.CardInfo("", "");
        var amount = generateValidAmount(firstCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.transferMoney(String.valueOf(amount), empty);
        assertAll(() -> transferPage.findErrorNotification("Ошибка! Произошла ошибка"),
                transferPage::cancelTransfer,
                () -> dashboardPage.reloadDashboardPage(),
                () -> assertEquals(firstCardBalance, dashboardPage.getCardBalance(0)),
                () -> assertEquals(secondCardBalance, dashboardPage.getCardBalance(1)));
    }

    @Test
    void shouldCancelTransferWithErrorIfCardInfoIsInvalid() {
        DataHelper.CardInfo empty = new DataHelper.CardInfo("1000 0000 0000 0001", "");
        var amount = generateValidAmount(firstCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.transferMoney(String.valueOf(amount), empty);
        assertAll(() -> transferPage.findErrorNotification("Ошибка! Произошла ошибка"),
                transferPage::cancelTransfer,
                () -> dashboardPage.reloadDashboardPage(),
                () -> assertEquals(firstCardBalance, dashboardPage.getCardBalance(0)),
                () -> assertEquals(secondCardBalance, dashboardPage.getCardBalance(1)));
    }

}
