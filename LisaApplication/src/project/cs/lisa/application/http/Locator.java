package project.cs.lisa.application.http;

public class Locator {

    public enum Type {

        BLUETOOTH ("btmac");

        private String mKey;

        private Type(String variable) {
            mKey = variable;
        }

        public String getKey() {
            return mKey;
        }

    }

    private Type mType;
    private String mLocator;

    public Locator(Type type, String locator) {
        mType = type;
        mLocator = locator;
    }

    public String getQueryKey() {
        return mType.getKey();
    }

    public String getQueryValue() {
        return mLocator;
    }

}
