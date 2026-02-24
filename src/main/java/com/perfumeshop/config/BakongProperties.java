package com.perfumeshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bakong")
public class BakongProperties {

    private Merchant merchant = new Merchant();

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }

    public static class Merchant {
        private String accountId;
        private String name;
        private String city;
        private String currency;

        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}