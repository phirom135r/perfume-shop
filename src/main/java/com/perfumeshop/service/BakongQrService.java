package com.perfumeshop.service;

import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.IndividualInfo;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * KHQR generator using Bakong KHQR SDK (sdk-java 1.0.0.12)
 * Works like sample project:
 *  - create IndividualInfo (setters)
 *  - BakongKHQR.generateIndividual(info)
 *  - res.getData().getQr(), res.getData().getMd5()
 */
@Service
public class BakongQrService {

    @Value("${bakong.merchant.name:Perfume Shop}")
    private String merchantName;

    @Value("${bakong.merchant.accountId:}")
    private String bakongAccountId;

    @Value("${bakong.merchant.city:PHNOM PENH}")
    private String city;

    private static final KHQRCurrency CURRENCY = KHQRCurrency.USD;

    public record KhqrResult(String khqrString, String md5) {
        // Compatibility for code that expects getters
        public String getKhqrString() { return khqrString; }
        public String getMd5() { return md5; }
    }

    public KhqrResult generate(BigDecimal amount, String billNumber) {
        if (amount == null) amount = BigDecimal.ZERO;
        if (billNumber == null) billNumber = "";

        IndividualInfo info = new IndividualInfo();
        info.setBakongAccountId(bakongAccountId);
        info.setMerchantName(merchantName);
        info.setMerchantCity(city);
        info.setCurrency(CURRENCY);
        info.setAmount(amount.doubleValue());
        info.setBillNumber(billNumber);

        KHQRResponse<KHQRData> res = BakongKHQR.generateIndividual(info);

        if (res == null || res.getData() == null) {
            throw new RuntimeException("Invalid KHQR response from SDK");
        }

        String qr = res.getData().getQr();
        String md5 = res.getData().getMd5();

        if (qr == null || qr.isBlank() || md5 == null || md5.isBlank()) {
            throw new RuntimeException("KHQR response missing qr/md5");
        }

        return new KhqrResult(qr, md5);
    }
}