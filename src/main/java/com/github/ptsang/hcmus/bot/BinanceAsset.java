package com.github.ptsang.hcmus.bot;

public class BinanceAsset implements Comparable<BinanceAsset> {
	private double worth;
	private double free;
	private String asset;

	@Override
	public int compareTo(BinanceAsset o) {
		return (int) ((int) o.getWorth() - worth);
	}

	@Override
	public String toString() {
		return String.format("%-5s 💎 free: %.8f, 💰 worth: <b>%.3f</b>", asset, free, worth);
	}

	public double getWorth() {
		return worth;
	}

	public void setWorth(double worth) {
		this.worth = worth;
	}

	public double getFree() {
		return free;
	}

	public void setFree(double free) {
		this.free = free;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}
}
