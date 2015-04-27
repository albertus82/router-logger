package it.albertus.router;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Threshold {

	public enum Type {
		NOT_EQUAL("<>", new String[] { "ne", "!=", "<>", "^=" }),
		LESS_THAN("<", new String[] { "lt", "<" }),
		LESS_OR_EQUAL("<=", new String[] { "le", "<=" }),
		EQUAL("=", new String[] { "eq", "==", "=" }),
		GREATER_OR_EQUAL(">=", new String[] { "ge", ">=" }),
		GREATER_THAN(">", new String[] { "gt", ">" });

		private final String description;
		private final Set<String> abbreviations = new HashSet<String>();

		private Type(String description, String[] abbreviations) {
			this.description = description;
			this.abbreviations.addAll(Arrays.asList(abbreviations));
		}

		public String getDescription() {
			return description;
		}

		public Set<String> getAbbreviations() {
			return abbreviations;
		}

		@Override
		public String toString() {
			return description;
		}

		public static Type findByName(String name) {
			if (name != null) {
				for (Type type : Type.values()) {
					if (type.name().equalsIgnoreCase(name.trim()) || type.abbreviations.contains(name.trim().toLowerCase())) {
						return type;
					}
				}
			}
			return null;
		}
	}

	private final Type type;
	private final String key;
	private final double value;

	public Threshold(Type type, String key, double value) {
		this.type = type;
		this.key = key;
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public String getKey() {
		return key;
	}

	public double getValue() {
		return value;
	}

	public boolean isReached(final double value) {
		switch (type) {
		case EQUAL:
			return value == this.value;
		case GREATER_OR_EQUAL:
			return value >= this.value;
		case GREATER_THAN:
			return value > this.value;
		case LESS_OR_EQUAL:
			return value <= this.value;
		case LESS_THAN:
			return value < this.value;
		case NOT_EQUAL:
			return value != this.value;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Threshold)) {
			return false;
		}
		Threshold other = (Threshold) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		}
		else if (!key.equals(other.key)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return key + ' ' + type + ' ' + value;
	}

}