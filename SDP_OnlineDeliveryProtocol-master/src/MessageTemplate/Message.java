package MessageTemplate;

import FSM.IMessage;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Message implements IMessage, Cloneable {
    public static class Types{
        public static int REGISTER_TO_SERVER = 0;
        public static int CONNECTED_SUCCESSFULL = 1;
        public static int LOGIN_REQUEST = 2;
        public static int LOGIN_SUCCESSFUL = 3;
        public static int REGISTRATION_REQUIRED = 4;
        public static int REGISTRATION_REQUEST = 5;
        public static int REGISTRATION_SUCCESSFUL = 6;
        public static int LOGIN_AFTER_REG = 7;
        public static int ASKING_FOR_ITEMS = 8;
        public static int SENDING_ITEMS = 9;
        public static int ADD_ITEM = 10;
        public static int ORDER_ITEM = 11;
        public static int DELETE_ITEM = 12;
        public static int WRITE_FEEDBACK = 13;
        public static int UNAUTHORISED = 14;

        public static int RESOLVE_DOMAIN_NAME = 300;

    }
    public static class Params{
        public static String DOMAIN = "domain_name";
        public static String IP = "ip_addr";
        public static String USERNAME = "username";
        public static String PASSWORD = "password";
        public static String EMAIL = "email";
        public static String ROLE = "role";
        public static String TOKEN = "token";
        public static String ITEMS = "items";
        public static String FEEDBACK = "feedback";
    }
    private int messageId;
    private int toId = 5; //TCP machine ID
    private int fromId;
    private String toAddress; //ip:port
    private String fromAddress;
    private String time;
    private Map<String, Object> msg = new HashMap<>();
    public Message(){
    }
    public Message(int messageId) {
        this.messageId = messageId;
    }
    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public void setTime(String vrijeme) {
        this.time = time;
    }

    @Override
    public int getToId() {
        return toId;
    }

    @Override
    public void setToId(int toId) {
        this.toId = toId;
    }

    @Override
    public int getFromId() {
        return fromId;
    }

    @Override
    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    @Override
    public String getToAddress() {
        return toAddress;
    }

    @Override
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    @Override
    public String getFromAddress() {
        return fromAddress;
    }

    @Override
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
    public Map<String, Object> getMsg(){
        return msg;
    }
    public void setMsg(HashMap<String, Object> msg){
        this.msg = msg;
    }
    public void addParam(String paramName, Object value){
        this.msg.put(paramName, value);
    }
    public Object getParam(String paramName, boolean object){return this.msg.get(paramName);}
    public String getParam(String paramName){return (String)this.msg.get(paramName);}
    @Override
    public void parseTransportMessage(byte[] messageData, int length) {
        ByteArrayInputStream input = new ByteArrayInputStream(messageData);
        XMLDecoder decoder = new XMLDecoder(input);
        Message m = (Message)decoder.readObject();
        decoder.close();
        messageId = m.getMessageId();
        fromAddress = m.getFromAddress();
        msg= m.getMsg();
    }

    @Override
    public byte[] buildTransportMessage() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(output);
        encoder.writeObject(this);
        encoder.close();
        return output.toByteArray();
    }

    @Override
    public boolean equals(IMessage message) {
        return message.getMessageId() == messageId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Message clone = (Message) super.clone();
        return clone;
    }
}
