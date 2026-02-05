package com.fabiocondo.payments.emola;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmolaService {

    private final EmolaSoapClient soapClient;

    @Value("${emola.username}")
    private String username;

    @Value("${emola.password}")
    private String password;

    @Value("${emola.partnerCode}")
    private String partnerCode;

    @Value("${emola.key}")
    private String key;

    public EmolaService(EmolaSoapClient soapClient) {
        this.soapClient = soapClient;
    }

    public String pushUssd(String msisdn, String amount, String message, String refNo) {

        String transId = UUID.randomUUID().toString().replace("-", "");

        String soapXml =
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                        "xmlns:web=\"http://webservice.bccsgw.viettel.com/\">" +
                        "<soapenv:Header/>" +
                        "<soapenv:Body>" +
                        "<web:gwOperation>" +
                        "<Input>" +
                        "<username>" + username + "</username>" +
                        "<password>" + password + "</password>" +
                        "<wscode>pushUssdMessage</wscode>" +

                        "<param name=\"partnerCode\" value=\"" + partnerCode + "\"/>" +
                        "<param name=\"msisdn\" value=\"" + msisdn + "\"/>" +
                        "<param name=\"smsContent\" value=\"" + message + "\"/>" +
                        "<param name=\"transAmount\" value=\"" + amount + "\"/>" +
                        "<param name=\"transId\" value=\"" + transId + "\"/>" +
                        "<param name=\"language\" value=\"pt\"/>" +
                        "<param name=\"refNo\" value=\"" + refNo + "\"/>" +
                        "<param name=\"key\" value=\"" + key + "\"/>" +

                        "<rawData/>" +
                        "</Input>" +
                        "</web:gwOperation>" +
                        "</soapenv:Body>" +
                        "</soapenv:Envelope>";

        return soapClient.sendSoap(soapXml);
    }
}
