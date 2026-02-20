package com.fabiocondo.payments.mpesa;

public class MpesaPaymentResponse {

    private String output_ResponseCode;
    private String output_ResponseDesc;
    private String output_TransactionID;
    private String output_ConversationID;
    private String output_ThirdPartyReference;

    public MpesaPaymentResponse() {
    }

    public boolean isSuccess() {
        return "INS-0".equals(this.output_ResponseCode);
    }

    public String getOutput_ResponseCode() {
        return output_ResponseCode;
    }

    public void setOutput_ResponseCode(String output_ResponseCode) {
        this.output_ResponseCode = output_ResponseCode;
    }

    public String getOutput_ResponseDesc() {
        return output_ResponseDesc;
    }

    public void setOutput_ResponseDesc(String output_ResponseDesc) {
        this.output_ResponseDesc = output_ResponseDesc;
    }

    public String getOutput_TransactionID() {
        return output_TransactionID;
    }

    public void setOutput_TransactionID(String output_TransactionID) {
        this.output_TransactionID = output_TransactionID;
    }

    public String getOutput_ConversationID() {
        return output_ConversationID;
    }

    public void setOutput_ConversationID(String output_ConversationID) {
        this.output_ConversationID = output_ConversationID;
    }

    public String getOutput_ThirdPartyReference() {
        return output_ThirdPartyReference;
    }

    public void setOutput_ThirdPartyReference(String output_ThirdPartyReference) {
        this.output_ThirdPartyReference = output_ThirdPartyReference;
    }
}