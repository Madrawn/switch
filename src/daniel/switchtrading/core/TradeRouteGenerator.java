package daniel.switchtrading.core;

import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import daniel.switchtrading.wrapper.ExchangeWrapper;
import daniel.switchtrading.wrapper.Yobit;

@objid("f90ac205-5de2-4c71-9fca-8ae85ef0176a")
public class TradeRouteGenerator {

	private int counter;

	@objid("2101ae0c-7489-456d-a5fb-87b06c8538c2")
	public Set<CurrencyPair> availablePairs = new THashSet<CurrencyPair>();

	@objid("f454a088-012a-47de-85fe-a2762d12bdf9")
	public Set<PricedTradeRoute> generatedTradeRoutes = new THashSet<PricedTradeRoute>();

	public static void main(String[] args) {
		/*
		 * ExchangeWrapper y; try { y = new Yobit(); Set<CurrencyPair>
		 * pairsToUse = y.getAllPairs(); pairsToUse.removeIf((x) ->
		 * !(x.getBaseCurrency().equals( new Currency("btc")) ||
		 * x.getBaseCurrency().equals(new Currency("eth")) /* ||
		 * x.getBaseCurrency().equals(new Currency("waves")) ||
		 * x.getBaseCurrency().equals(new Currency("usd")) ||
		 * x.getBaseCurrency().equals(new Currency("rur")) || x
		 * .getBaseCurrency().equals(new Currency("doge"))));
		 * y.getTradeBooks(pairsToUse); Set<CurrencyPair> cleanPairs =
		 * y.cleanDeadPairs(); TradeRouteGenerator trg = new
		 * TradeRouteGenerator(cleanPairs);
		 * trg.mixAll(Integer.parseInt(args[0]), new Currency("btc")); //
		 * System.out.println(trg);
		 * 
		 * TreeMap<Double, TradeRoute> profitRoutes = new TreeMap<Double,
		 * TradeRoute>();
		 * 
		 * Set<CurrencyPair> badPairs = new THashSet<>(); int counter = 0; for
		 * (PricedTradeRoute route : trg.generatedTradeRoutes) { counter++; if
		 * (counter % 1000 == 0) { System.out.println(String.format("%s/%s",
		 * counter, trg.generatedTradeRoutes.size()));
		 * 
		 * } if (!route.contains(badPairs)) { try { TradeRouteEvaluator tre =
		 * new DepthTradeRouteEvaluator( y);
		 * 
		 * BigDecimal in = 0.001; route.setInAmount(in); double out =
		 * tre.evaluate(route,false); if (out == 0) { // remove all routes
		 * containing the problem currency } if (in < out) {
		 * profitRoutes.put(out, route); System.out.println("Found! " + out +
		 * "\n" + route); } } catch (TooFewPositionsException e) {
		 * e.printStackTrace(); System.out.println("Adding " +
		 * e.getCauseCurrency()); if (!badPairs.add(e.getCauseCurrency()))
		 * System.out.println("Already added " + e.getCauseCurrency());
		 * System.out.println("Number of bad pairs = " + badPairs.size());
		 * 
		 * } } else { // System.out.println("Skipped because of bad Pair"); ; }
		 * 
		 * } System.out.println("FINISHED");
		 * 
		 * profitRoutes.forEach((d, r) -> {
		 * System.out.println(String.format("%s -----------\n%s", d,
		 * ((PricedTradeRoute) r))); });
		 * 
		 * } catch (MalformedURLException e) { // TODO Auto-generated catch
		 * blocky e.printStackTrace(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } catch (Exception e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
	}

	@objid("d06e4885-36c6-4548-b43a-338eeccab67c")
	public TradeRouteGenerator(final Set<CurrencyPair> pairsToUse) {
		this.availablePairs = pairsToUse;
	}

	@objid("6ded7e18-c328-48bc-b303-ec009b9d4242")
	public void mixAll(final int numHops, Currency start) {
		List<PricedTradeStep> allPossibleTradeSteps = new ArrayList<PricedTradeStep>();

		for (CurrencyPair currencyPair : availablePairs) {
			if (currencyPair.toString().equals("bs_btc")) {
				System.out.println("How did you get in here?");
			}
			allPossibleTradeSteps.add(new PricedTradeStep(currencyPair
					.getBaseCurrency(), currencyPair.getSecondCurrency(),
					currencyPair, BigDecimal.ZERO));
			allPossibleTradeSteps.add(new PricedTradeStep(currencyPair
					.getSecondCurrency(), currencyPair.getBaseCurrency(),
					currencyPair, BigDecimal.ZERO));
		}

		Set<PricedTradeRoute> workTradeRoutes = new THashSet<>();
		for (PricedTradeStep tradeStep : allPossibleTradeSteps) {
			if (tradeStep.from.equals(start)) {
				workTradeRoutes.add(new PricedTradeRoute(tradeStep));
			}
		}

		// build hops
		for (int i = 0; i < numHops - 2; i++) {
			Set<PricedTradeRoute> tmpTradeRoutes = new THashSet<>();

			int size = workTradeRoutes.size();

			workTradeRoutes
					.stream()
					.forEach(
							(tradeRoute) -> {
								allPossibleTradeSteps
										.stream()
										.forEach(
												(tradeStep) -> {
													if (!tradeStep.to
															.equals(start)
															&& tradeRoute
																	.getTailCurrency()
																	.equals(tradeStep.from)) {
														PricedTradeRoute tr = new PricedTradeRoute(
																tradeRoute
																		.getList());
														try {
															if (tmpTradeRoutes
																	.size() % 1000 == 0 && tmpTradeRoutes.size()>0) {

																System.out
																		.println(String
																				.format("TradeRoutes: %s",
																						size));
																System.out
																		.println(String
																				.format("TradeRoutes: %s",
																						tmpTradeRoutes
																								.size()));
															}
															if (tmpTradeRoutes
																	.size() == 2495371) {
																System.out
																		.println("Why wat");
															}
															tr.addStep(tradeStep);
														} catch (Exception e) {
															// we're already
															// checking for it
															// so should be fine
															e.printStackTrace();
														}
														tmpTradeRoutes.add(tr);
													}
												});
							});
			/*
			 * for (TradeRoute tradeRoute : workTradeRoutes) { for (TradeStep
			 * tradeStep : allPossibleTradeSteps) { if
			 * (!tradeStep.to.equals(start) &&
			 * tradeRoute.getTailCurrency().equals( tradeStep.from)) {
			 * PricedTradeRoute tr = new PricedTradeRoute(
			 * tradeRoute.getList()); try { if (tmpTradeRoutes.size() % 1000 ==
			 * 0) {
			 * 
			 * System.out.println(String.format( "TradeRoutes: %s",
			 * workTradeRoutes.size())); System.out.println(String.format(
			 * "TradeRoutes: %s", tmpTradeRoutes.size())); } if
			 * (tmpTradeRoutes.size() == 2495371) { System.out.println("Why"); }
			 * tr.addStep(tradeStep); } catch (Exception e) { // we're already
			 * checking for it so should be fine e.printStackTrace(); }
			 * tmpTradeRoutes.add(tr); } }
			 * 
			 * }
			 */
			workTradeRoutes = tmpTradeRoutes;
		}
		generatedTradeRoutes = workTradeRoutes;
		// add last hop back
		{
			counter = 0;

			generatedTradeRoutes
					.stream()
					.forEach(
							(tradeRoute) -> {
								counter++;

								if (counter % 10000 == 0) {
									System.out.println(String.format("%s / %s",
											counter,
											generatedTradeRoutes.size()));
									if (counter == 4380000) {
										System.out
												.println("Why you break here");

									}

								}

								try {
									CurrencyPair relevantPair = new CurrencyPair(
											tradeRoute.getTailCurrency(), start);
									
									if (!availablePairs.contains(relevantPair)) {
										relevantPair = new CurrencyPair(start,
												tradeRoute.getTailCurrency());
										if (!availablePairs
												.contains(relevantPair)) {
											relevantPair = new CurrencyPair(
													tradeRoute
															.getTailCurrency(),
													start);
										}
									}
									//we should get the minOrdersize
									for (CurrencyPair currencyPair : availablePairs) {
										if (relevantPair.equals(currencyPair)) {
											relevantPair = currencyPair;
										}
									}
									tradeRoute.addStep(new PricedTradeStep(
											tradeRoute.getTailCurrency(),
											start, relevantPair, BigDecimal.ZERO));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
		}
		/*
		 * for (PricedTradeRoute tradeRoute : generatedTradeRoutes) { counter++;
		 * 
		 * if (counter % 10000 == 0) {
		 * System.out.println(String.format("%s / %s", counter,
		 * generatedTradeRoutes.size())); }
		 * 
		 * try { tradeRoute.addStep(new PricedTradeStep(tradeRoute
		 * .getTailCurrency(), start, new CurrencyPair(tradeRoute
		 * .getTailCurrency(), start), 0)); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } }
		 */
		//System.out.println("Why");

	}

	public void mixAll(Currency start) {
		mixAll(3, start);
	}

	@Override
	public String toString() {
		String result = "Pairs: " + availablePairs.size() + "\n";
		for (TradeRoute tradeRoute : generatedTradeRoutes) {
			result += tradeRoute.toString() + "\n";
		}
		return result;
	}
}
