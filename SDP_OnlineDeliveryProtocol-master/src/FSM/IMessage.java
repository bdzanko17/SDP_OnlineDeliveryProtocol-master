package FSM;

public interface IMessage extends Cloneable {
	public int getMessageId();
	public void setMessageId(int messageId);
	
	public String getTime();
	public void setTime(String vrijeme);
	
	public int getToId();
	public void setToId(int toId);
	public int getFromId();
	public void setFromId(int fromId);
	
	public String getToAddress();
	public void setToAddress(String toAddress);
	public String getFromAddress();
	public void setFromAddress(String fromAddress);
	
	public void parseTransportMessage(byte[] messageData, int length);
	public byte[] buildTransportMessage();
	
	public boolean equals(IMessage message);
	public Object clone() throws CloneNotSupportedException;
}
