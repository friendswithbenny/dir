package org.fwb.file;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.Arrays;
import java.util.Iterator;

public class ByteSizeFormat extends DecimalFormat {
	private static final long serialVersionUID = 1;
	
	static final String DEFAULT = "#,##0.0";
	
	public ByteSizeFormat() {
		this(DEFAULT);
	}
	ByteSizeFormat(String decimalFormat) {
		super(decimalFormat);
	}
	
	@Override
	public StringBuffer format(long l, StringBuffer sb, FieldPosition fp) {
		ByteMagnitude bm = magnitude(l);
		return super.format(bm.COEF, sb, fp).append(bm.SIZE.name());
	}
	
	static ByteMagnitude magnitude(long l) {
		Iterator<ByteSize> i = Arrays.asList(ByteSize.values()).iterator();
		ByteSize bs = i.next();
		while (l >= ByteSize.FACTOR && i.hasNext()) {	// len test is MUCH (~100%) more likely to fail than i.hasNext() (unless size > 1024 PetaByte!)
			bs = i.next();
			l /= ByteSize.FACTOR;
		}
		return new ByteMagnitude(l, bs);
	}
	
	static class ByteMagnitude {
		final long COEF;
		final ByteSize SIZE;
		ByteMagnitude(long coefficient, ByteSize units) {
			COEF = coefficient;
			SIZE = units;
		}
	}
	
//	/** @deprecated unused */
//	private static final long
//		BYTE = 1,
//		KB = 1024,
//		MB = KB * KB,
//		GB = KB * MB,
//		TB = KB * GB,
//		PB = KB * TB;
	enum ByteSize {
		B(),
		K(B),
		M(K),
		G(M),
		T(G),
		P(T);
		
		static final int
			ZERO = 0,
			ONE = 1,
			FACTOR = 1024;
		
		/** power of {@link #FACTOR} */
		final int POWER;
		final long VALUE;
		ByteSize(int power) {
			POWER = power;
			VALUE = ((long) FACTOR) ^ POWER;
		}
		ByteSize() {
			this(ZERO);
		}
		ByteSize(ByteSize previous) {
			this(ONE + previous.POWER);
		}
	}
}
