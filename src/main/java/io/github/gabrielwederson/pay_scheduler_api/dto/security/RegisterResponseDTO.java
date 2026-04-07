package io.github.gabrielwederson.pay_scheduler_api.dto.security;

import java.math.BigDecimal;
import java.util.List;

public class RegisterResponseDTO {
    private Long id;
    private String email;
    private String name;
    private List<AccountSummaryDTO> accounts;

    public RegisterResponseDTO(Long id, String email, String name, List<AccountSummaryDTO> accounts) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.accounts = accounts;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public List<AccountSummaryDTO> getAccounts() { return accounts; }

    public static class AccountSummaryDTO {
        private Long id;
        private String numberAccount;
        private BigDecimal balance;
        private BigDecimal availableBalance;

        public AccountSummaryDTO(Long id, String numberAccount, BigDecimal balance, BigDecimal availableBalance) {
            this.id = id;
            this.numberAccount = numberAccount;
            this.balance = balance;
            this.availableBalance = availableBalance;
        }

        public Long getId() { return id; }
        public String getNumberAccount() { return numberAccount; }
        public BigDecimal getBalance() { return balance; }
        public BigDecimal getAvailableBalance() { return availableBalance; }
    }
}
