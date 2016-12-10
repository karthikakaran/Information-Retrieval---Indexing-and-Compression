//===========================================================================================================================
//	Program : Class for calculating time
//===========================================================================================================================
//	@author: Karthika Karunakaran
// 	Date created: 2016/10/26
//===========================================================================================================================
public class Timer {
	long startTime, endTime, elapsedTime, memAvailable, memUsed;

	public Timer() {
		startTime = System.currentTimeMillis();
	}

	public void start() {
		startTime = System.currentTimeMillis();
	}

	public Timer end() {
		endTime = System.currentTimeMillis();
		elapsedTime = endTime - startTime;
		memAvailable = Runtime.getRuntime().totalMemory();
		memUsed = memAvailable - Runtime.getRuntime().freeMemory();
		return this;
	}

	public String toString() {
		return "Time: " + elapsedTime + " msec.\n";
	}

}
