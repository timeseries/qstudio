package com.timestored.sqldash.chart;

/**
 * Contains java equivalent for KDB functions.
 */
class KdbFunctions {

	/** Add amount to every nums and return new result **/
	static double[] mul(double[] nums, double amount) {
		double[] res = new double[nums.length];
		for(int i=0; i<nums.length; i++) {
			res[i] = nums[i] * amount;
		}
		return res;
	}
	
	/** Add amount to every nums and return new result **/
	static double[] add(double[] nums, double amount) {
		double[] res = new double[nums.length];
		for(int i=0; i<nums.length; i++) {
			res[i] = nums[i] + amount;
		}
		return res;
	}

	/** Add amount to every nums and return new result **/
	static double[] add(double[] nums, double[] amount) {
		double[] res = new double[nums.length];
		for(int i=0; i<nums.length; i++) {
			res[i] = nums[i] + amount[i];
		}
		return res;
	}

	/** same as KDB's til function **/
	static double[] til(int X) {
		double[] r = new double[X];
		for(int i=0; i<X; i++) {
			r[i] = i;
		}
		return r;
	}

	/** perform cosine on every elements of array and return new array with result. **/
	static double[] cos(double[] nums) {
		double[] res = new double[nums.length];
		for(int i=0; i<nums.length; i++) {
			res[i] = Math.cos(nums[i]);
		}
		return res;
	}

	/** perform cosine on every elements of array and return new array with result. **/
	static double[] sin(double[] nums) {
		double[] res = new double[nums.length];
		for(int i=0; i<nums.length; i++) {
			res[i] = Math.sin(nums[i]);
		}
		return res;
	}

	/** perform modulus on every elements of array and return new array with result. **/
	static double[] mod(double[] nums, double modder) {
		double[] res = new double[nums.length];
		for(int i=0; i<nums.length; i++) {
			res[i] = nums[i] % modder;
		}
		return res;
	}
}
