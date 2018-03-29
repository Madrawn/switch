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

import daniel.switchtrading.wrapper.Bitfinex;
import daniel.switchtrading.wrapper.ExchangeWrapper;

@objid("03713d1d-54c9-4ebe-a27b-5c110e0f7b6a")
public class TradeCircle extends Frame implements ActionListener, Runnable {

	public static void main(String[] args) {
		try {
			ExchangeWrapper exchange = (ExchangeWrapper) Class.forName(
					"daniel.switchtrading.wrapper." + args[2]).newInstance();

			if (exchange instanceof Bitfinex) {
				try {

					exchange.getTradeBooks(exchange.getAllPairs());
					wrapper = exchange;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			startCur = init(args);

			BookKeeperThread target2 = new BookKeeperThread(exchange, numHops,
					startCur, inAmount);
			Thread t = new Thread(target2, "BookKeeper");
			t.setDaemon(true);
			t.start();
			TradeCircle target = new TradeCircle();
			target2.addListener(target);

			// SwingUtilities.invokeLater(target);

			Thread c = new Thread(target, "TradeCircle");
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

	private static Currency startCur;

	/**
	 * @param args
	 * @return
	 */
	public static Currency init(String[] args) {
		Currency startCur = new Currency(args[0]);
		numHops = Integer.parseInt(args[1]);
		exchangeName = args[2];
		try {
			if (wrapper == null) {

				wrapper = (ExchangeWrapper) Class.forName(
						"daniel.switchtrading.wrapper." + exchangeName)
						.newInstance();
				wrapper.setupAuth();
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
		}
		inAmount = new BigDecimal(Double.parseDouble(args[3]));
		isAutomated = Boolean.parseBoolean(args[4]);
		return startCur;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized (tradesToCheck) {

			PricedTradeRoute source = (PricedTradeRoute) e.getSource();
			// System.out.println("Putting new route");
			if (tradesToCheck.contains(source)) {
				tradesToCheck.remove(source);
			}else {
				System.out.println("putting brand new route!");
			}
			int freeCap = tradesToCheck.remainingCapacity();
			// System.out.println("Free Capacity: " + freeCap);
			if (freeCap == 0) {
				tradesToCheck.removeLast();
			}
			tradesToCheck.offerFirst(source);
		}
	}

	@Override
	public void run() {
		while (!stop) {
			try {

				// TODO: check all routes at once
				tradesToCheck.offer(tradesToCheck.take());
				PricedTradeRoute toCheckRoute = checkAndGetMostProfitable(tradesToCheck);
				
				boolean isStillProfitable = false;
				if (toCheckRoute != null) {
					isStillProfitable = checkRouteProfit(toCheckRoute);
					
				}
				if (isStillProfitable) {
					tradesToCheck.remove(toCheckRoute);
					tradesToCheck.offerFirst(toCheckRoute);
					execute(toCheckRoute);
				} else {
				}
				// shouldn't we do this in the wrapper?
				// Thread.sleep(100);

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

				List<Trade> trades = step.getTrades();
				//
				BigDecimal priceThreshold = trades.get(trades.size() - 1).price;
				BigDecimal amountSum = BigDecimal.ZERO;
				for (Trade trade : trades) {
					amountSum = amountSum.add(trade.in);

				}
				Wallet wallet = wrapper.getUserBalance().getWallet(step.from);
				// int tolerance = 1;

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
				System.out.println("Free Balance = " + wallet.freeBalance
						+ wallet.currency);

				// If we're not trading in the "base" ie BTC currency we can
				// sell all
				BigDecimal amount;
				if (step.from.equals(startCur)) {
					amount = amountSum.min(amountInWallet);

				} else {
					amount = amountInWallet;

				}

				placeTrade(step, trades, priceThreshold, amount);

				abort = waitForFinishedTrade(toCheckRoute, step,
						step.getOutSum(), priceThreshold);
				if (abort) {
					// try to get rid of left overs
					// only if we're not already in the start currency
					if (!step.from.equals(startCur)) {

						try {
							// try to clear all from left
							CurrencyPair relevantPair;
							relevantPair = wrapper.getAccordingPair(step.from,
									startCur);
							PricedTradeStep fakeStep = new PricedTradeStep(
									step.from, startCur, relevantPair,
									BigDecimal.ONE);
							wallet = wrapper.getUserBalance().getWallet(
									step.from);
							amount = wallet.freeBalance;
							placeTrade(fakeStep, trades, priceThreshold, amount);

							// if we stopped in the middle we also need to get
							// rid of the to left

							if (!step.to.equals(startCur)) {
								relevantPair = wrapper.getAccordingPair(
										step.to, startCur);
								fakeStep = new PricedTradeStep(step.to,
										startCur, relevantPair, BigDecimal.ONE);
								wallet = wrapper.getUserBalance().getWallet(
										step.to);
								amount = wallet.freeBalance;
								placeTrade(fakeStep, trades, priceThreshold,
										amount);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			} else {
				System.out.println("\nABORTING!\n");
				if (wrapper != null) {
					// wrapper.closeAllTrades();

				}
			}
		}
	}

	/**
	 * @param toCheckRoute
	 * @param abort
	 * @param step
	 * @param amountSum
	 * @param oldPriceThreshold
	 * @param wallet
	 * @return
	 * @throws InterruptedException
	 */
	private boolean waitForFinishedTrade(PricedTradeRoute toCheckRoute,
			PricedTradeStep step, BigDecimal amountSum,
			BigDecimal oldPriceThreshold) throws InterruptedException {
		int counter = 0;
		boolean abort = false;
		System.out.println("CurrentStep: " + step);
		System.out.println("Waiting for: " + amountSum);
		Wallet wallet = wrapper.getUserBalance().getWallet(step.to);
		Wallet walletFrom = wrapper.getUserBalance().getWallet(step.from);
		// we should have about this much
		if (wallet != null) {
			System.out.println("Wallet.freebalance: " + wallet.freeBalance);
			
		}
		System.out.println("WalletFrom.freebalance: " + walletFrom.freeBalance);
		BigDecimal toHave = step.getOutSum();
		System.out.println("ToHave: " + toHave);
		BigDecimal minFrom = step.inAmount.multiply(new BigDecimal(0.1));
		System.out.println("MinFrom: " + minFrom);
		System.out.println("(walletFrom.freeBalance.compareTo(minFrom) <= 0): " + (walletFrom.freeBalance.compareTo(minFrom) <= 0));
		System.out.println("(wallet == null || wallet.freeBalance.compareTo(toHave.multiply(new BigDecimal(0.8))) <= 0): " + (wallet == null || wallet.freeBalance.compareTo(toHave.multiply(new BigDecimal(0.8))) <= 0));
		while ((wallet == null || wallet.freeBalance.compareTo(toHave
				.multiply(new BigDecimal(0.8))) <= 0)
			&& (!(walletFrom.freeBalance.compareTo(minFrom) <= 0)||counter < 5)) {
			System.out.println("Waitng for funds " + amountSum);
			if (wallet != null) {
				System.out.println("Currently we have: " + wallet.freeBalance
						+ " " + wallet.currency);
			}
			Thread.sleep(100);
			// tolerance--;
			counter++;
			if (counter >= 10) {
				System.out.println(String.format(
						"We have been here %s times something went wrong",
						counter));
				if (counter >= 20) {
					// System.exit(0);
					abort = true;
				}

			}
			try {
				checkRouteProfit(toCheckRoute);
				int index = toCheckRoute.getList().indexOf(step);
				PricedTradeStep lastStep = toCheckRoute.getList().get(index);
				// TODO: this is debug information and uses up valuable trade
				// time
				BigDecimal priceThreshold = lastStep.getTrades().get(
						lastStep.getTrades().size() - 1).price;
				abort = (priceThreshold.compareTo(oldPriceThreshold
						.multiply(new BigDecimal(0.8))) < 0) || abort;
				if (!abort) {
					// tolerance = 5;
					if (wrapper instanceof Bitfinex) {
						// bitfinex is market order so no retrade necceary
					} else {
						lastOrder = retrade(toCheckRoute, lastOrder, step);
					}
				} else if (abort) {

					break;

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			wallet = wrapper.getUserBalance().getWallet(step.to);
			walletFrom = wrapper.getUserBalance().getWallet(step.from);

		}
		System.out.println("Wait is over we have: " + wallet.freeBalance);
		return abort;
	}

	private OpenOrder retrade(PricedTradeRoute toCheckRoute,
			OpenOrder lastOrder, PricedTradeStep step) {

		System.out.println("Retrading");
		int index = toCheckRoute.getList().indexOf(step);
		PricedTradeStep lastStep = toCheckRoute.getList().get(index);
		BigDecimal priceThreshold = lastStep.getTrades().get(
				lastStep.getTrades().size() - 1).price;
		// TODO: this is debug information and uses up valuable trade time
		try {
			TradeBook tb = wrapper.getTradeBook(lastStep.relevantPair);
			BigDecimal bestAsk = tb.getAsks().first().getPrice();
			BigDecimal bestBid = tb.getBids().first().getPrice();
			
			//we should replace the price
			if (lastStep.from.equals(lastStep.relevantPair.getBaseCurrency())) {
				priceThreshold = bestAsk;
			} else {
				priceThreshold = bestBid;
				
			}
			
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
			// this is bullshit because remaining is already the right number
			// because we get it from yobit
			if (lastStep.from.equals(lastStep.relevantPair.getBaseCurrency())) {
				remaining = remaining.multiply(priceThreshold);
			}

			BigDecimal amountInWallet = wallet.freeBalance;

			final BigDecimal amount = remaining.min(amountInWallet);
			// wrapper.setupAuth();
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
			/*
			 * ExchangeWrapper altWrapper = (ExchangeWrapper) Class.forName(
			 * "daniel.switchtrading.wrapper." + exchangeName) .newInstance();
			 */
			wrapper.getTradeBooks(pairsToCheck);
			DepthTradeRouteEvaluator dp = new DepthTradeRouteEvaluator(wrapper);

			// check routes with new tradebook and remember best

			BigDecimal bestOut = BigDecimal.ZERO;
			for (PricedTradeRoute tradeRoute : toCheck) {
				try {

					BigDecimal curOut = dp.evaluate(tradeRoute, false);
					if (bestOut.compareTo(curOut) <= 0) {
						bestOut = curOut;
						best = tradeRoute;
					}
				} catch (TooFewPositionsException e) {
					tradesToCheck.remove(toCheck);
				} catch (java.net.SocketTimeoutException e) {
					//TODO: skip for now
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
		// System.out.println("Checked " + toCheck.size() + " routes");
		return best;
	}

	private boolean checkRouteProfit(PricedTradeRoute toCheckRoute)
			throws Exception {

		DepthTradeRouteEvaluator eval = new DepthTradeRouteEvaluator(wrapper);

		toCheckRoute.resetSteps();
		toCheckRoute.setInAmount(inAmount);
		BigDecimal out = eval.evaluate(toCheckRoute, true);
		BigDecimal divide = out.divide(inAmount, MathContext.DECIMAL32);
		if (divide.compareTo(wrapper.getFeeTolerance()) > 0) {
			System.out.println(divide);
			System.out.println("Route still profitable");
			System.out.println(String.format("%.9f --> %.9f", inAmount, out));
			return true;
		}
		// System.out.println("Route not profitable");
		// System.out.println(String.format("%.9f --> %.9f", inAmount, out));
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
