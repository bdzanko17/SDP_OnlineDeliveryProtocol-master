package FSM;

import FSM.pa.SequenceChart;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Klasa zaduzena za koordinaciju komunikacije svih FSM-ova u sistemu.
 * 
 * @author Enio Kaljic, mr.el.-dipl.ing.el.<br>
 *         Odsjek za telekomunikacije, ETF Sarajevo
 */
public class Dispatcher extends Frame implements Runnable {
	private ArrayList<IFSM> fsmList = new ArrayList<IFSM>();
	Queue<IMessage> redcekanja = new LinkedList<IMessage>();
	private Queue<IMessage> messageQueue = new LinkedList<IMessage>();
	private boolean running;
	private SequenceChart chart;
	private boolean debugEnabled;

	/**
	 * Konstruktor klase
	 * 
	 * @param debugEnabled
	 *            parametar koji sluzi za omogucavanje prikaza MSC dijagrama
	 */
	public Dispatcher(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
		if (debugEnabled) {
			// Prikaz MSC dijagrama
			chart = new SequenceChart();
			add("Center", chart);
			pack();
			setTitle("FSM Library Protocol Analyzer - Sequence Chart");
			setSize(800, 600);
			setVisible(true);
		}
	}

	/**
	 * Metoda za pokretanje Dipatcher-a i svih FSM-ova u sistemu.
	 */
	public void start() {
		if (!running) {
			running = true;
			// Prolazak kroz listu FSM-ova pomocu iteratora
			Iterator<IFSM> it = fsmList.iterator();
			while (it.hasNext()) {
				// Pokretanje FSM-a
				IFSM fsm = it.next();
				fsm.start();
			}
			// Pokretanje Dispatcher-a
			Thread runningThread = new Thread(this);
			runningThread.start();
		}
	}

	/**
	 * Metoda za zaustavljanje Dispatcher-a i svih FSM-ova u sistemu.
	 */
	public void stop() {
		if (running) {
			// Zaustavljanje Dispatcher-a
			running = false;
			// Prolazak kroz listu FSM-ova pomocu iteratora
			Iterator<IFSM> it = fsmList.iterator();
			while (it.hasNext()) {
				// Zaustavljanje FSM-a
				IFSM fsm = it.next();
				fsm.stop();
			}
		}
	}

	/**
	 * Metoda za dodavanje FSM-a u sistem.
	 * 
	 * @param fsm
	 *            instanca klase koja implementira interfejs IFSM.
	 */
	public void addFSM(IFSM fsm) {
		// Dodjela Dispatcher-a FSM-u
		fsm.setDispatcher(this);
		// Dodavanje FSM-a u sistem
		fsmList.add(fsm);
		if (debugEnabled) {
			// Dodavanje FSM-a na MSC dijagram
			chart.addFSM(fsm);
		}
	}

	/**
	 * Metoda za preuzimanje FSM-a na osnovu njegovog identifikatora.
	 * 
	 * @param id
	 *            identifikator klase koja implementira interfejs IFSM.
	 * @return Instanca klase koja implementira interfejs IFSM.
	 */
	public IFSM getFSM(int id) {
		// Prolazak kroz listu FSM-ova pomocu iteratora
		Iterator<IFSM> it = fsmList.iterator();
		while (it.hasNext()) {
			IFSM fsm = it.next();
			// Provjera FSM-a na osnovu identifikatora
			if (fsm.getId() == id) {
				return fsm;
			}
		}
		return null;
	}

	/**
	 * Metoda za dodavanje poruke u red cekanja Dispatcher-a.
	 * 
	 * @param message
	 *            instanca klase koja implementira interfejs IMessage.
	 */
	public void addMessage(IMessage message) {
		messageQueue.add(message);
		
	}
	
	public Queue<IMessage> getQueue(){
		
		return this.redcekanja;
	}
	
	
	

	/**
	 * Metoda za obradu poruka pristiglih u red cekanja Dispatcher-a.
	 * 
	 * @see Runnable#run()
	 */
	@Override
	public void run() {
		while (running) {
			if (messageQueue.size() != 0) {
				// Preuzimanje poruke iz reda cekanja
				IMessage message = messageQueue.poll();
				redcekanja.add(message);
				
				
				// Preuzimanje FSM-a na kojeg je adresirana poruka
				IFSM fsm = getFSM(message.getToId());
				if (fsm != null) {
					// Spremanje poruke u red cekanja FSM-a
					fsm.addMessage(message);
					if (debugEnabled) {
						// Prikaz poruke na MSC dijagramu
						chart.addMessage(message);
					}
				}
			}
			// Pauziranje izvrsavanja na 1 us kako bi se smanjilo opterecenje CPU-a
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
}
