package daniel.switchtrading.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import daniel.switchtrading.wrapper.ExchangeWrapper;

public class BookKeeperThread implements Runnable {

	private ExchangeWrapper exchange;

	public BookKeeperThread(ExchangeWrapper exchange, int numHops,
			Currency startCurrency, BigDecimal inAmount2) {
		this.exchange = exchange;
		this.numHops = numHops;
		this.startCurrency = startCurrency;
		this.inAmount = inAmount2;
	}

	public boolean stop;
	private Currency startCurrency;
	private int numHops;
	private Vector<ActionListener> listeners = new Vector<>();
	private Set<CurrencyPair> badPairs = new HashSet<>();
	private BigDecimal inAmount;

	public void addListener(ActionListener p) {
		listeners.addElement(p);
	}

	@Override
	public void run() {

		while (!stop) {
			try {

				Set<CurrencyPair> allPairs = null;
				try {
					exchange.refresh();
					allPairs = exchange.getAllPairs();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					exchange.getTradeBooks(allPairs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Set<CurrencyPair> cleanPairs = exchange.cleanDeadPairs();
				TradeRouteGenerator trg = new TradeRouteGenerator(cleanPairs);
				trg.mixAll(numHops, startCurrency);
				// System.out.println(trg);

				TreeMap<BigDecimal, TradeRoute> profitRoutes = new TreeMap<BigDecimal, TradeRoute>();

				int counter = 0;
				for (PricedTradeRoute route : trg.generatedTradeRoutes) {
					counter++;
					if (counter % 1000 == 0) {
						System.out.println(String.format("%s/%s", counter,
								trg.generatedTradeRoutes.size()));

					}
					boolean notFoundBadPair = (!route.contains(badPairs) && !route
							.contains(exchange.deadPairs));
					if (notFoundBadPair) {
						try {
							TradeRouteEvaluator tre = new DepthTradeRouteEvaluator(
									exchange);

							BigDecimal in = inAmount;
							route.setInAmount(in);
							BigDecimal out = tre.evaluate(route, false);
							if (out.compareTo(BigDecimal.ZERO) == 0) {
								// remove all routes containing the problem
								// currency
							}
							if (in.compareTo(out) < 0) {
								profitRoutes.put(out, route);

								for (ActionListener actionListener : listeners) {
									ActionEvent e = new ActionEvent(route, 0,
											"foundRoute" + out);
									actionListener.actionPerformed(e);
								}

								// System.out.println("Found! " + out + "\n" +
								// route);
							}
						} catch (TooFewPositionsException e) {
							// e.printStackTrace();
							System.out
									.println("Adding " + e.getCauseCurrency());
							if (!badPairs.add(e.getCauseCurrency())) {
								System.out.println("Already added "
										+ e.getCauseCurrency());

								System.out.println("SanityCheck: "
										+ route.contains(badPairs) + " "
										+ route.contains(e.getCauseCurrency()));
							}
							System.out.println("Number of bad pairs = "
									+ badPairs.size());

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						// System.out.println("Skipped because of bad Pair");
						;
					}

				}

			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("BookKeeperThread almost died");
			}
		}
	}

}
