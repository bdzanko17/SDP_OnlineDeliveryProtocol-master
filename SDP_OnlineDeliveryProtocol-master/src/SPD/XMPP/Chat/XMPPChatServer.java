package SPD.XMPP.Chat;

import FSM.IFSM;
import FSM.FSM;
import MessageTemplate.Message;
import FSM.TcpTransportServer;
import FSM.Dispatcher;
import FSM.IMessage;

import java.util.ArrayList;
import java.util.Scanner;
public class XMPPChatServer extends FSM implements IFSM {
    public XMPPChatServer(int id) {
        super(id);
    }
    ArrayList<Users> users = new ArrayList<Users>();
    ArrayList<Items> items = new ArrayList<Items>();
    ArrayList<String> feedbacks = new ArrayList<String>();

    static int READY = 0;



    @Override
    public void init() {
        addTransition(READY, new Message(Message.Types.REGISTER_TO_SERVER), "onServerRegister");
        addTransition(READY, new Message(Message.Types.LOGIN_REQUEST), "checkRegisterLogin");
        addTransition(READY, new Message(Message.Types.REGISTRATION_REQUEST), "checkRegister");
        addTransition(READY, new Message(Message.Types.LOGIN_AFTER_REG), "checkRegisterLogin");
        addTransition(READY, new Message(Message.Types.ASKING_FOR_ITEMS), "sendingItems");
        addTransition(READY, new Message(Message.Types.ADD_ITEM), "addingItems");
        addTransition(READY, new Message(Message.Types.ORDER_ITEM), "orderingItems");
        addTransition(READY, new Message(Message.Types.DELETE_ITEM), "deletionItems");
        addTransition(READY, new Message(Message.Types.WRITE_FEEDBACK), "getFeedback");


    }

    public void onServerRegister(IMessage message){
        Message msg = (Message) message;
        Message response = new Message(Message.Types.CONNECTED_SUCCESSFULL);
        response.setToAddress(msg.getFromAddress());
        sendMessage(response);
    }
    public void checkRegisterLogin(IMessage message){
        Message msg = (Message) message;
        Users user = new Users(msg.getParam(Message.Params.USERNAME), msg.getParam(Message.Params.EMAIL), msg.getParam(Message.Params.PASSWORD), msg.getParam(Message.Params.ROLE));
        System.out.println(user);
        System.out.println(msg.getParam("username") + msg.getParam("email"));
        boolean found = false;
        for (Users u : users) {
            if(user.equals(u)) {
                found = true;
                break;
            }
        }
        if(found){
            Message response = new Message(Message.Types.LOGIN_SUCCESSFUL);
            response.setToAddress(msg.getFromAddress());
            sendMessage(response);
            System.out.println("Login on server.");
        } else {
            Message response = new Message(Message.Types.REGISTRATION_REQUIRED);
            response.setToAddress(msg.getFromAddress());
            sendMessage(response);
            System.out.println("Registration on server.");
        }
    }
    public void checkRegister(IMessage message){
        Message msg = (Message) message;
        Users user = new Users(msg.getParam(Message.Params.USERNAME), msg.getParam(Message.Params.EMAIL), msg.getParam(Message.Params.PASSWORD), msg.getParam(Message.Params.ROLE));
        users.add(user);
        Message response = new Message(Message.Types.REGISTRATION_SUCCESSFUL);
        response.setToAddress(msg.getFromAddress());
        sendMessage(response);
    }

    //
    // //
    // //
    // //
    //

    public void sendingItems(IMessage message){
        Message msg = (Message) message;
        Items item = new Items ("Samsung", 10);
        items.add(item);
        System.out.println(items);
        Message response = new Message(Message.Types.SENDING_ITEMS);
        response.setToAddress(msg.getFromAddress());
        response.addParam(Message.Params.ITEMS, items);
        sendMessage(response);
    }
    public void addingItems(IMessage message){
        Message msg = (Message) message;
        Items item = new Items();
        String role = msg.getParam(Message.Params.ROLE);
        if(!role.equals("admin")){
            Message response = new Message(Message.Types.UNAUTHORISED);
            response.setToAddress(msg.getFromAddress());
            sendMessage(response);
            return;
        }
        item = (Items) msg.getParam(Message.Params.ITEMS, true);
        items.add(item);
        System.out.println("ITEM ADDED");
        Message response = new Message(Message.Types.SENDING_ITEMS);
        response.setToAddress(msg.getFromAddress());
        response.addParam(Message.Params.ITEMS, items);
        sendMessage(response);
    }
    public void orderingItems(IMessage message){
        Message msg = (Message) message;
        Items item = new Items();
        item = (Items) msg.getParam(Message.Params.ITEMS, true);
        int s =0;
        for (int i=0;i<items.size();i++){
            if(item.equals(items.get(i))){
                Integer is =items.get(i).getCOUNT()-1;
                items.get(i).setCOUNT(is);
            }
        }
        Message response = new Message(Message.Types.SENDING_ITEMS);
        response.setToAddress(msg.getFromAddress());
        response.addParam(Message.Params.ITEMS, items);
        System.out.println("ITEM ORDERED");
        sendMessage(response);
    }
    public void deletionItems(IMessage message){
        Message msg = (Message) message;
        final Items item = (Items) msg.getParam(Message.Params.ITEMS, true);
        items.removeIf(s->s.equals(item));
        System.out.println("Item deleted :)");
        Message response = new Message(Message.Types.SENDING_ITEMS);
        response.setToAddress(msg.getFromAddress());
        items.forEach(items1 -> {System.out.println(items1);});
        response.addParam(Message.Params.ITEMS, items);
        sendMessage(response);

        /*boolean found = false;
        for (Items u : items) {
            if(user.equals(u)) {
                found = true;
                break;
            }
        }
        if(found){
            Message response = new Message(Message.Types.LOGIN_SUCCESSFUL);
            response.setToAddress(msg.getFromAddress());
            sendMessage(response);
            System.out.println("Login on server.");
        } else {
            Message response = new Message(Message.Types.REGISTRATION_REQUIRED);
            response.setToAddress(msg.getFromAddress());
            sendMessage(response);
            System.out.println("Registration on server.");
        }*/
    }

    public void getFeedback(IMessage message) {
        Message msg = (Message) message;
        String feedback = msg.getParam(Message.Params.FEEDBACK);
        System.out.println(feedback);
    }

    static int SERVER_PORT = 9999;
    public static void main(String[] args) throws Exception{
	// write your code here
        XMPPChatServer XMPPChatServerFSM = new XMPPChatServer(0);
        TcpTransportServer tcpFSM = new TcpTransportServer(5);
        tcpFSM.setServerPort(SERVER_PORT);
        tcpFSM.setReceiver(XMPPChatServerFSM);

        Dispatcher dis = new Dispatcher(false);
        dis.addFSM(XMPPChatServerFSM);
        dis.addFSM(tcpFSM);
        dis.start();

        System.out.println("Server is running on port 9999!");
        while(true){
            Thread.sleep(1);
        }
    }
}
