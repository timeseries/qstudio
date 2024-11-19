package com.timestored.qstudio.kdb;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import kx.c.Minute;
import kx.c.Month;
import kx.c.Second;
import kx.c.Timespan;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * Represents Kdb Data types, provides acces to their null/infinty values.
 */
enum KdbType {
	
	BOOLEAN(-1, Boolean.class, 'b'),
	GUID(-2, UUID.class, 'g', new UUID(0, 0)),
	BYTE(-4, Byte.class, 'x', new Byte((byte) 0)),
	SHORT(-5, Short.class, 'h', new Short(Short.MIN_VALUE), new Short((short)-(1+Short.MIN_VALUE)), new Short((short)(1+Short.MIN_VALUE))), 
	INT(-6, Integer.class, 'i', new Integer(Null.I), new Integer(Inf.I), new Integer(NegInf.I)),
	LONG(-7, Long.class, 'j', new Long(Null.J), new Long(Inf.J), new Long(NegInf.J)),
	REAL(-8, Float.class, 'e', new Float(Null.E), new Float(Inf.E), new Float(NegInf.E)),
	FLOAT(-9, Double.class, 'f', new Double(Null.F), new Double(Inf.F), new Double(NegInf.F)),
	CHAR(-10, Character.class, 'c', new Character(' ')),
	SYMBOL(-11, String.class, 's', ""),
	TIMESTAMP(-12, Timestamp.class, 'p', new Timestamp(Null.J)),
	MONTH(-13, Month.class, 'm', new Month(Null.I), new Month(Inf.I), new Month(NegInf.I)),
	DATE(-14, Date.class, 'd', new Date(Null.J), new Date(Inf.J), new Date(NegInf.J)),
	DATETIME(-15, java.util.Date.class, 'z', new java.util.Date(Null.J), new java.util.Date(Inf.J), new java.util.Date(NegInf.J)),
	TIMESPAN(-16, Timespan.class, 'n', new Timespan(Null.J), new Timespan(Inf.J), new Timespan(NegInf.J)),
	MINUTE(-17, Minute.class, 'u', new Minute(Null.I), new Minute(Inf.I), new Minute(NegInf.I)),
	SECOND(-18, Second.class, 'v', new Second(Null.I), new Second(Inf.I), new Second(NegInf.I)),
	TIME(-19, Time.class, 't', new Time(Null.J), new Time(Inf.J), new Time(NegInf.J));

		
	KdbType(int typeNum, @SuppressWarnings("rawtypes") Class clas, char ch) {
		this(typeNum, clas, ch, null, null, null);
	}

	private KdbType(int typeNum, @SuppressWarnings("rawtypes") Class clas, 
			char ch, Object nullVal) {
		this(typeNum, clas, ch, nullVal, null, null);
	}

		private KdbType(int typeNum, @SuppressWarnings("rawtypes") Class clas, char ch,
				Object nullVal, Object posInfinity, Object negInfinity) {
		
		this.typeNum = typeNum;
		this.clas = clas;
		this.characterCode = ch;
		this.nullValue = nullVal;
		this.posInfinity = posInfinity;
		this.negInfinity = negInfinity;
	}


	private final Class<?> clas;
	private final char characterCode;
	private final int typeNum;
	private final Object nullValue;
	private final Object posInfinity;
	private final Object negInfinity;

	private static final Map<Class<?>, KdbType> classLookup = Maps.uniqueIndex(
			ImmutableList.copyOf(KdbType.values()),
			new com.google.common.base.Function<KdbType, Class<?>>() {
				public Class<?> apply(KdbType kt) {return kt.getClas();}
			});

	/** @return true of this type supportsd infinites otherwise false */
	public boolean hasInfinity() {
		return posInfinity!=null;
	}
	
	public char getCharacterCode() {
		return characterCode;
	}
	
	public Class<?> getClas() {
		return clas;
	}
	
	public Object getNullValue() {
		return nullValue;
	}
	
	public Object getNegInfinity() {
		return negInfinity;
	}
	
	public Object getPosInfinity() {
		return posInfinity;
	}
	
	public int getTypeNum() {
		return typeNum;
	}

	/*
	 * Storing a whole bunch of constants to check for infinities
	 */
	private static final long INF_TIME_LONG = 2143883647l;
	private static final int INF_TIMESTAMP_NANOS = 854775807;
	private static final long INF_TIMESTAMP_TIME = 10170053236854l;
	private static final long INF_DATE_LONG = 185543533782000000l;
	private static final long NEG_DATE_LONG = -185541640416000000l;
	private static final long INF_DATE = -9223371090169975809l;
	private static final long NEG_TIMESTAMP_TIME = -8276687236855l;	
	private static final long NEG_TIMESTAMP_NANOS = 145224193l;
	private static final long NEG_TIME_LONG = -2151083647l;
	private static final long NEG_DATE = -9223371090169975808l;

	/** For a given class return the KdbType or null if none apply */
	public static KdbType getType(Class<?> clas) {
		return classLookup.get(clas);
	}
	
	/** @return true if the object o is a null in KDB. */
	public static boolean isNull(Object o) {
		if(o!=null) {
			for(KdbType kt : values()) {
				if(o.equals(kt.nullValue)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** @return true if the object o is a positive infinity in KDB.  */
	public static boolean isPositiveInfinity(Object o) {
		// these classes don't have a proper equals so need checked manually
		if(o instanceof Date) {
			return ((Date) o).getTime() == INF_DATE_LONG;
		} else if(o instanceof Timestamp) {
			Timestamp ts = (Timestamp) o;
			return ts.getTime() == INF_TIMESTAMP_TIME
					&& ts.getNanos() == INF_TIMESTAMP_NANOS; 
		} else if(o instanceof Time) {
			return ((Time) o).getTime() == INF_TIME_LONG;
		} else if(o instanceof java.util.Date) {
			return ((java.util.Date) o).getTime() == INF_DATE;
		} 
		
		if(o!=null) {
			for(KdbType kt : values()) {
				if(o.equals(kt.posInfinity)) {
					return true;
				}
			}
		}
		
		return false;
	}
	

	
	/** @return true if the object o is a negative infinity in KDB. */
	public static boolean isNegativeInfinity(Object o) {// these classes don't have a proper equals so need checked manually
		if(o.getClass() == Date.class) {
			return ((Date) o).getTime() == NEG_DATE_LONG;
		} else if(o.getClass() == Timestamp.class) {
			Timestamp ts = (Timestamp) o;
			return ts.getTime() == NEG_TIMESTAMP_TIME
					&& ts.getNanos() == NEG_TIMESTAMP_NANOS; 
		} else if(o instanceof Time) {
			return ((Time) o).getTime() == NEG_TIME_LONG;
		} else if(o instanceof java.util.Date) {
			return ((java.util.Date) o).getTime() == NEG_DATE;
		} 

		if(o!=null) {
			for(KdbType kt : values()) {
				if(o.equals(kt.negInfinity)) {
					return true;
				}
			}
		}
		return false;
	}
}

/*
 * hacky method of getting shortcodes for static final variables
 */

final class Null {
	public static final int I = Integer.MIN_VALUE;
	public static final long J = Long.MIN_VALUE;	
	public static final float E = Float.NaN;
	public static final double F = Double.NaN;
}

final class Inf {

	public static final int I = Integer.MAX_VALUE;
	public static final long J = Long.MAX_VALUE;
	public static final float E = Float.POSITIVE_INFINITY;
	public static final double F = Double.POSITIVE_INFINITY;
}

final class NegInf {
	public static final int I = 1+Integer.MIN_VALUE;
	public static final long J = 1+Long.MIN_VALUE;
	public static final double E = -Inf.E;
	public static final double F = -Inf.F;
}