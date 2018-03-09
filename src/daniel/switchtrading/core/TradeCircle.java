package daniel.switchtrading.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.SynchronousQueue;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.wrapper.ExchangeWrapper;

@objid("03713d1d-54c9-4ebe-a27b-5c110e0f7b6a")
public class TradeCircle implements ActionListener, Runnable{
	
	private static double inAmount;
	private static String exchangeName;
	SynchronousQueue<PricedTradeRoute> tradesToCheck = new SynchronousQueue<>();

	public static void main(String[] args) {
		// baseCur numHops exchange
		Currency startCur = new Currency(args[0]);
		int numHops = Integer.parseInt(args[1]);
		try {
			ExchangeWrapper exchange = (ExchangeWrapper) Class.forName(
					"daniel.switchtrading.wrapper." + args[2]).newInstance();
			
			Thread t = new Thread(new BookKeeperThread(exchange, numHops, startCur));
			t.setDaemon(true);
			t.start();
			inAmount = Double.parseDouble(args[3]);
			Thread c = new Thread(new TradeCircle());			
			exchangeName = args[2];
			c.setDaemon(false);
			c.start();
			
			
			
			
			
			
			
			
			
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean stop;

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			tradesToCheck.put((PricedTradeRoute) e.getSource());
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(!stop){
			try {
				PricedTradeRoute toCheckRoute = tradesToCheck.take();
				boolean isStillProfitable = checkRouteProfit(toCheckRoute);
				if (isStillProfitable) {
					System.out.println("We would act now");
				}
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean checkRouteProfit(PricedTradeRoute toCheckRoute) throws Exception {
		
		DepthTradeRouteEvaluator eval = new DepthTradeRouteEvaluator((ExchangeWrapper) Class.forName(
					"daniel.switchtrading.wrapper." + exchangeName).newInstance());
		
		toCheckRoute.setInAmount(inAmount);
		double out = eval.evaluate(toCheckRoute);
		if (inAmount<out) {
			return true;
		}
		
		
		return false;
	}

}
