package com.juhe.creditcardapplyfor.bo;


import java.io.Serializable;

/**
 * @author Administrator
 * @title: Message
 * @projectName creditcard
 * @description: TODO
 * @date 2019/7/26 17:30
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;


    private String errCode;
    private String message;
    private Object data;

    public Message(String errCode, String message, Object data) {
        this.errCode = errCode;
        this.message = message;
        this.data = data;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "errCode='" + errCode + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
