package android.studio.provider;

public enum MediaType {

	NONE, IMAGE, AUDIO, VIDEO, DOCUMENT, APK, COMPRESS;

	public static MediaType get(String name) {
		for (MediaType type : values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return NONE;
	}
}
