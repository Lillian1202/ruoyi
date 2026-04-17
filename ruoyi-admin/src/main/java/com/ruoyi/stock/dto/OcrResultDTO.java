package com.ruoyi.stock.dto;

public class OcrResultDTO {
    private String ocrText;
    private String qrCode;
    private boolean ocrSuccess;
    private boolean qrSuccess;
    private String ocrError;
    private String qrError;

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public boolean isOcrSuccess() {
        return ocrSuccess;
    }

    public void setOcrSuccess(boolean ocrSuccess) {
        this.ocrSuccess = ocrSuccess;
    }

    public boolean isQrSuccess() {
        return qrSuccess;
    }

    public void setQrSuccess(boolean qrSuccess) {
        this.qrSuccess = qrSuccess;
    }

    public String getOcrError() {
        return ocrError;
    }

    public void setOcrError(String ocrError) {
        this.ocrError = ocrError;
    }

    public String getQrError() {
        return qrError;
    }

    public void setQrError(String qrError) {
        this.qrError = qrError;
    }
}
