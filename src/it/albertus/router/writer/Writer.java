package it.albertus.router.writer;

import java.util.Map;

public interface Writer {

	enum Destination {
		NONE(0),
		CSV(1),
		DATABASE(2);

		private final int id;

		private Destination(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public static Destination getEnum(String id) {
			if (id != null) {
				try {
					int code = Integer.parseInt(id);
					for (Destination destination : Destination.values()) {
						if (destination.id == code) {
							return destination;
						}
					}
				}
				catch (Exception e) {
				}
			}
			return null;
		}
	}

	void saveInfo(Map<String, String> info);

	void release();

}
