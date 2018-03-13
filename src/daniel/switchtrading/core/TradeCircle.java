package daniel.switchtrading.core;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.wrapper.ExchangeWrapper;

@objid("03713d1d-54c9-4ebe-a27b-5c110e0f7b6a")
public class TradeCircle extends Frame implements ActionListener, Runnable {

	private static final Comparator<? super PricedTradeRoute> comparator = (x1,
			x2) -> {
		// get out of both routes
		double out1 = x1.getList().get(x1.getList().size() - 1).getOutSum();
		double out2 = x2.getList().get(x2.getList().size() - 1).getOutSum();

		return Double.compare(out2, out1);
	};

	private static boolean isAutomated;
	private static double inAmount;
	private static String exchangeName;
	LinkedBlockingDeque<PricedTradeRoute> tradesToCheck = new LinkedBlockingDeque<PricedTradeRoute>(
			10);

	public static void main(String[] args) {
		// baseCur numHops exchange
		Currency startCur = init(args);
		try {
			ExchangeWrapper exchange = (ExchangeWrapper) Class.forName(
					"daniel.switchtrading.wrapper." + args[2]).newInstance();

			BookKeeperThread target2 = new BookKeeperThread(exchange, numHops,
					startCur, inAmount);
			Thread t = new Thread(target2);
			t.setDaemon(true);
			t.start();
			TradeCircle target = new TradeCircle();
			target2.addListener(target);

			// SwingUtilities.invokeLater(target);

			Thread c = new Thread(target);
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

	/**
	 * @param args
	 * @return
	 */
	public static Currency init(String[] args) {
		Currency startCur = new Currency(args[0]);
		numHops = Integer.parseInt(args[1]);
		exchangeName = args[2];
		inAmount = Double.parseDouble(args[3]);
		isAutomated = Boolean.parseBoolean(args[4]);
		return startCur;
	}

	private static boolean stop;
	private static int numHops;
	private ExchangeWrapper wrapper;

	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized (tradesToCheck) {

			System.out.println("Putting new route");
			int freeCap = tradesToCheck.remainingCapacity();
			System.out.println("Free Capacity: " + freeCap);
			if (freeCap == 0) {
				tradesToCheck.removeLast();
			}
			tradesToCheck.offerFirst((PricedTradeRoute) e.getSource());
		}
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				
				//TODO: check all routes at once
				PricedTradeRoute toCheckRoute = tradesToCheck.take();
				boolean isStillProfitable = checkRouteProfit(toCheckRoute);
				if (isStillProfitable) {

					execute(toCheckRoute);
					tradesToCheck.offerFirst(toCheckRoute);
				} else {
					tradesToCheck.offer(toCheckRoute);
				}
				Thread.sleep(500);

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

	public void execute(PricedTradeRoute toCheckRoute)
			throws InvocationTargetException, InterruptedException {
		boolean abort = false;
		System.out.println("Executing: " + toCheckRoute.toString());
		OpenOrder lastOrder = null;
		for (PricedTradeStep step : toCheckRoute.getList()) {
			if (!abort) {

				try {
					wrapper = (ExchangeWrapper) Class.forName(
							"daniel.switchtrading.wrapper." + exchangeName)
							.newInstance();

					wrapper.setupAuth();

					List<Trade> trades = step.getTrades();
					double priceThreshold = trades.get(trades.size() - 1).price;
					double amountSum = 0;
					for (Trade trade : trades) {
						amountSum += trade.in;

					}
					Wallet wallet = wrapper.getUserBalance().getWallet(
							step.from);
					int tolerance = 1;
					while (wallet == null
							|| wallet.freeBalance <= amountSum * 0.8) {
						System.out.println("Waitng for funds " + amountSum);
						if (wallet != null) {
							System.out.println("Currently we have: "
									+ wallet.freeBalance);
						}
						Thread.sleep(500);
						tolerance--;
						try {
							checkRouteProfit(toCheckRoute);
							if (!abort && tolerance == 0) {
								tolerance = 5;
								lastOrder = retrade(toCheckRoute, lastOrder,
										step);
							} else if (abort) {

								break;

							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						wallet = wrapper.getUserBalance().getWallet(step.from);
					}

					if (abort) {
						wrapper.closeOrder(lastOrder.ID);
						break;
					}

					double amountInWallet = wallet.freeBalance;

					final double amount = Math.min(amountSum, amountInWallet);

					// we can't trade if its too small
					/*
					 * if (amount / priceThreshold < 0.0001) { abort = true;
					 * break; }
					 */
					if (isAutomated) {
						lastOrder = wrapper.doTrade(step.from, step.to,
								priceThreshold, amount, step.relevantPair);
					} else {
						EventQueue.invokeAndWait(new Runnable() {

							@Override
							public void run() {
								String message;
								if (trades.get(0).fromBase) {
									message = String
											.format("Confirm to buy %s with %s %s for %s %s",
													step.to, amount, step.from,
													priceThreshold,
													step.relevantPair);

								} else {
									message = String
											.format("Confirm to sell %s %s gaining %s for %s %s",
													amount, step.from, step.to,
													priceThreshold,
													step.relevantPair);

								}
								int answer = JOptionPane.showConfirmDialog(
										null, message, "Confirm trade!",
										JOptionPane.OK_CANCEL_OPTION);
								if (answer == JOptionPane.YES_OPTION) {
									wrapper.doTrade(step.from, step.to,
											priceThreshold, amount,
											step.relevantPair);
								} else {
									System.out.println("Stopping trading");
									TradeCircle.stop = true;
									System.exit(0);
									return;
								}
							}
						});

					}
					System.out.println("Trade step finished");
					// out trade could have gone through our trade could have
					// been partially filled our trade could be stuck.

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

			} else {
				System.out.println("\nABORTING!\n");
				if (wrapper != null) {
					wrapper.closeAllTrades();

				}
			}
		}
	}

	private OpenOrder retrade(PricedTradeRoute toCheckRoute,
			OpenOrder lastOrder, PricedTradeStep step) {
		
		System.out.println("Retrading");
		int index = toCheckRoute.getList().indexOf(step) - 1;
		if (wrapper.closeOrder(lastOrder.ID)) {

			PricedTradeStep lastStep = toCheckRoute.getList().get(index);
			Wallet wallet = wrapper.getUserBalance().getWallet(lastStep.from);
			double priceThreshold = lastStep.getTrades().get(
					lastStep.getTrades().size() - 1).price;
			double remaining = lastOrder.position.getSize();
			if (lastStep.from.equals(lastStep.relevantPair.getBaseCurrency())) {
				remaining = remaining * priceThreshold;
			}
			double amountInWallet = wallet.freeBalance;

			final double amount = Math.min(remaining, amountInWallet);

			return wrapper.doTrade(lastStep.from, lastStep.to, priceThreshold,
					amount, lastStep.relevantPair);
		}
		return null;
	}

	private boolean checkRouteProfit(PricedTradeRoute toCheckRoute)
			throws Exception {

		DepthTradeRouteEvaluator eval = new DepthTradeRouteEvaluator(
				(ExchangeWrapper) Class.forName(
						"daniel.switchtrading.wrapper." + exchangeName)
						.newInstance());

		toCheckRoute.setInAmount(inAmount);
		double out = eval.evaluate(toCheckRoute, true);
		if (inAmount < out) {
			System.out.println("Route still profitable");
			System.out.println(String.format("%.9f --> %.9f", inAmount, out));
			return true;
		}
		System.out.println("Route not profitable");
		return false;
	}

}
