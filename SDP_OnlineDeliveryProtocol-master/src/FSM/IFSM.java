package FSM;

public interface IFSM {
	public void setId(int id);
	public int getId();
	public void start();
	public void stop();
	public void setDispatcher(Dispatcher dispatcher);
	public Dispatcher getDispatcher();
	public void addMessage(IMessage message);
}
