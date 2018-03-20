package daniel.switchtrading.core;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JOptionPane;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.wrapper.ExchangeWrapper;

@objid("03713d1d-54c9-4ebe-a27b-5c110e0f7b6a")
public class TradeCircle extends Frame implements ActionListener, Runnable {

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

	private static final Comparator<? super PricedTradeRoute> comparator = (x1,
			x2) -> {
		// get out of both routes
		BigDecimal out1 = x1.getList().get(x1.getList().size() - 1).getOutSum();
		BigDecimal out2 = x2.getList().get(x2.getList().size() - 1).getOutSum();

		return out2.compareTo(out1);
	};

	private static boolean isAutomated;
	private static BigDecimal inAmount;
	private static String exchangeName;
	LinkedBlockingDeque<PricedTradeRoute> tradesToCheck = new LinkedBlockingDeque<PricedTradeRoute>(
			100);

	private static boolean stop;

	private static int numHops;

	private static ExchangeWrapper wrapper;

	private OpenOrder lastOrder;

	/**
	 * @param args
	 * @return
	 */
	public static Currency init(String[] args) {
		Currency startCur = new Currency(args[0]);
		numHops = Integer.parseInt(args[1]);
		exchangeName = args[2];
		try {
			wrapper = (ExchangeWrapper) Class.forName(
					"daniel.switchtrading.wrapper." + exchangeName)
					.newInstance();
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
		inAmount = new BigDecimal(Double.parseDouble(args[3]));
		isAutomated = Boolean.parseBoolean(args[4]);
		return startCur;
	}

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

				// TODO: check all routes at once
				tradesToCheck.offer(tradesToCheck.take());
				PricedTradeRoute toCheckRoute = checkAndGetMostProfitable(tradesToCheck);

				boolean isStillProfitable = checkRouteProfit(toCheckRoute);
				if (isStillProfitable) {
					tradesToCheck.remove(toCheckRoute);
					tradesToCheck.offerFirst(toCheckRoute);
					execute(toCheckRoute);
				} else {
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

	@SuppressWarnings("finally")
	public void execute(PricedTradeRoute toCheckRoute)
			throws InvocationTargetException, InterruptedException {
		boolean abort = false;
		System.out.println("Executing: " + toCheckRoute.toString());
		for (PricedTradeStep step : toCheckRoute.getList()) {
			if (!abort) {

				wrapper.setupAuth();

				List<Trade> trades = step.getTrades();
				BigDecimal priceThreshold = trades.get(trades.size() - 1).price;
				BigDecimal amountSum = BigDecimal.ZERO;
				for (Trade trade : trades) {
					amountSum = amountSum.add(trade.in);

				}
				Wallet wallet = wrapper.getUserBalance().getWallet(step.from);
				// int tolerance = 1;
				int counter = 0;
				while (wallet == null
						|| wallet.freeBalance.compareTo(amountSum
								.multiply(new BigDecimal(0.8))) <= 0) {
					System.out.println("Waitng for funds " + amountSum);
					if (wallet != null) {
						System.out.println("Currently we have: "
								+ wallet.freeBalance);
					}
					Thread.sleep(500);
					// tolerance--;
					counter++;
					if (counter >= 10) {
						System.out
								.println(String
										.format("We have been here %s times something went wrong",
												counter));
						if (counter >= 20) {
							// System.exit(0);
							abort = true;
						}

					}
					try {
						checkRouteProfit(toCheckRoute);
						if (!abort) {
							// tolerance = 5;
							lastOrder = retrade(toCheckRoute, lastOrder, step);
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
					try {

						wrapper.closeOrder(lastOrder.ID);
					} catch (Exception e) {
						// TODO: handle exception
					} finally {
						break;
					}
				}

				BigDecimal amountInWallet = wallet.freeBalance;

				final BigDecimal amount = amountSum.min(amountInWallet);

				placeTrade(step, trades, priceThreshold, amount);

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
		PricedTradeStep lastStep = toCheckRoute.getList().get(index);
		// TODO: this is debug information and uses up valuable trade time
		BigDecimal priceThreshold = lastStep.getTrades().get(
				lastStep.getTrades().size() - 1).price;
		try {
			TradeBook tb = wrapper.getTradeBook(lastStep.relevantPair);
			BigDecimal bestAsk = tb.getAsks().first().getPrice();
			BigDecimal bestBid = tb.getBids().first().getPrice();
			if (bestAsk == bestBid) {
				Thread.dumpStack();
				System.out.println("this should never be the case");
				checkRouteProfit(toCheckRoute);

			}
			int compHalf = priceThreshold
					.divide(bestAsk, MathContext.DECIMAL32).compareTo(
							new BigDecimal(0.5));
			int compTwo = priceThreshold.divide(bestAsk, MathContext.DECIMAL32)
					.compareTo(new BigDecimal(2));
			if (compTwo > 0 || compHalf < 0) {
				System.out.println("Should look into this");
			}
			System.out.println(String.format(
					"According to the tradeBook bestBid: %s bestAsk: %s",
					bestBid, bestAsk));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BigDecimal remaining = lastOrder.position.getSize();
		System.out.println(String.format(
				"Trying to buy %s for the price of %s", remaining,
				priceThreshold));

		if (wrapper.closeOrder(lastOrder.ID)) {

			Wallet wallet = wrapper.getUserBalance().getWallet(lastStep.from);
			if (lastStep.from.equals(lastStep.relevantPair.getBaseCurrency())) {
				remaining = remaining.multiply(priceThreshold);
			}
			BigDecimal amountInWallet = wallet.freeBalance;

			final BigDecimal amount = remaining.min(amountInWallet);

			return wrapper.doTrade(lastStep.from, lastStep.to, priceThreshold,
					amount, lastStep.relevantPair);
		} else {
			System.out.println(String.format(
					"Attempt to close order %s failed", lastOrder.ID));
		}
		return lastOrder;
	}

	private PricedTradeRoute checkAndGetMostProfitable(
			LinkedBlockingDeque<PricedTradeRoute> toCheck) {

		// first get all contained pairs
		Set<CurrencyPair> pairsToCheck = new HashSet<>();
		for (PricedTradeRoute pricedTradeRoute : toCheck) {
			pairsToCheck.addAll(pricedTradeRoute.getCurrencyPairsContained());
		}
		PricedTradeRoute best = null;

		try {
			// now get relevant tradebooks with a new wrapper so we get most
			// recent books
			ExchangeWrapper altWrapper = (ExchangeWrapper) Class.forName(
					"daniel.switchtrading.wrapper." + exchangeName)
					.newInstance();
			altWrapper.getTradeBooks(pairsToCheck);
			DepthTradeRouteEvaluator dp = new DepthTradeRouteEvaluator(
					altWrapper);

			// check routes with new tradebook and remember best

			BigDecimal bestOut = BigDecimal.ZERO;
			for (PricedTradeRoute tradeRoute : toCheck) {
				try {

					BigDecimal curOut = dp.evaluate(tradeRoute, false);
					if (bestOut.compareTo(curOut) < 0) {
						bestOut = curOut;
						best = tradeRoute;
					}
				} catch (TooFewPositionsException e) {
					tradesToCheck.remove(toCheck);
				}

			}

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
		System.out.println("Checked " + toCheck.size() + " routes");
		return best;
	}

	private boolean checkRouteProfit(PricedTradeRoute toCheckRoute)
			throws Exception {

		DepthTradeRouteEvaluator eval = new DepthTradeRouteEvaluator(wrapper);

		toCheckRoute.resetSteps();
		toCheckRoute.setInAmount(inAmount);
		BigDecimal out = eval.evaluate(toCheckRoute, true);
		BigDecimal divide = out.divide(inAmount, MathContext.DECIMAL32);
		System.out.println(divide);
		if (divide.compareTo(new BigDecimal(1.003)) > 0) {
			System.out.println("Route still profitable");
			System.out.println(String.format("%.9f --> %.9f", inAmount, out));
			return true;
		}
		System.out.println("Route not profitable");
		System.out.println(String.format("%.9f --> %.9f", inAmount, out));
		return false;
	}

	/**
	 * @param step
	 * @param trades
	 * @param priceThreshold
	 * @param amount
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	private void placeTrade(PricedTradeStep step, List<Trade> trades,
			BigDecimal priceThreshold, final BigDecimal amount)
			throws InterruptedException, InvocationTargetException {
		// we can't trade if its too small
		/*
		 * if (amount / priceThreshold < 0.0001) { abort = true; break; }
		 */
		if (isAutomated) {
			lastOrder = wrapper.doTrade(step.from, step.to, priceThreshold,
					amount, step.relevantPair);
		} else {
			EventQueue.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					String message;
					if (trades.get(0).fromBase) {
						message = String.format(
								"Confirm to buy %s with %s %s for %s %s",
								step.to, amount, step.from, priceThreshold,
								step.relevantPair);

					} else {
						message = String.format(
								"Confirm to sell %s %s gaining %s for %s %s",
								amount, step.from, step.to, priceThreshold,
								step.relevantPair);

					}
					int answer = JOptionPane.showConfirmDialog(null, message,
							"Confirm trade!", JOptionPane.OK_CANCEL_OPTION);
					if (answer == JOptionPane.YES_OPTION) {
						wrapper.doTrade(step.from, step.to, priceThreshold,
								amount, step.relevantPair);
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
	}

}
